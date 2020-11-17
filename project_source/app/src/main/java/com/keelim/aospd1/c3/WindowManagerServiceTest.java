package com.keelim.aospd1.c3;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArraySet;
import android.view.Choreographer;
import android.view.DisplayCutout;
import android.view.WindowManager;

import com.keelim.aospd1.R;
import com.keelim.aospd1.c4.WindowManagerPolicy;
import com.keelim.test.after.WindowManagerGlobal;
import com.keelim.test.after.WindowState;
import com.keelim.test.after.WindowToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import static android.view.WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW;
import static android.view.WindowManager.LayoutParams.FIRST_SUB_WINDOW;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
import static android.view.WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
import static android.view.WindowManager.LayoutParams.LAST_SUB_WINDOW;
import static android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG;
import static android.view.WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_DREAM;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_QS_DIALOG;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_VOICE_INTERACTION;

public class WindowManagerServiceTest {
    private HashMap<Integer, Function<WindowToken, Integer>> map;

    private WindowManagerServiceTest(Context context, InputManagerService inputManager,
                                     boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy,
                                     ActivityTaskManagerService atm, TransactionFactory transactionFactory) {
        installLock(this, INDEX_WINDOW);
        mGlobalLock = atm.getGlobalLock();
        mAtmService = atm;
        mContext = context;
        mAllowBootMessages = showBootMsgs;
        mOnlyCore = onlyCore;
        mLimitedAlphaCompositing = context.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_limitedAlpha);
        mHasPermanentDpad = context.getResources().getBoolean(
                com.android.internal.R.bool.config_hasPermanentDpad);
        mInTouchMode = context.getResources().getBoolean(
                com.android.internal.R.bool.config_defaultInTouchMode);
        mDrawLockTimeoutMillis = context.getResources().getInteger(
                com.android.internal.R.integer.config_drawLockTimeoutMillis);
        mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(
                com.android.internal.R.bool.config_allowAnimationsInLowPowerMode);
        mMaxUiWidth = context.getResources().getInteger(
                com.android.internal.R.integer.config_maxUiWidth);
        mDisableTransitionAnimation = context.getResources().getBoolean(
                com.android.internal.R.bool.config_disableTransitionAnimation);
        mPerDisplayFocusEnabled = context.getResources().getBoolean(
                com.android.internal.R.bool.config_perDisplayFocusEnabled);
        mLowRamTaskSnapshotsAndRecents = context.getResources().getBoolean(
                com.android.internal.R.bool.config_lowRamTaskSnapshotsAndRecents);
        mInputManager = inputManager; // Must be before createDisplayContentLocked.
        mDisplayManagerInternal = LocalServices.getService(DisplayManagerInternal.class);
        mDisplayWindowSettings = new DisplayWindowSettings(this);

        mTransactionFactory = transactionFactory;
        mTransaction = mTransactionFactory.make();
        mPolicy = policy;
        mAnimator = new WindowAnimator(this);
        mRoot = new RootWindowContainer(this);

        mWindowPlacerLocked = new WindowSurfacePlacer(this);
        mTaskSnapshotController = new TaskSnapshotController(this);

        mWindowTracing = WindowTracing.createDefaultAndStartLooper(this,
                Choreographer.getInstance());

        LocalServices.addService(WindowManagerPolicy.class, mPolicy);

        mDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

        mKeyguardDisableHandler = KeyguardDisableHandler.create(mContext, mPolicy, mH);

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mPowerManagerInternal = LocalServices.getService(PowerManagerInternal.class);

        if (mPowerManagerInternal != null) {
            mPowerManagerInternal.registerLowPowerModeObserver(
                    new PowerManagerInternal.LowPowerModeListener() {
                        @Override
                        public int getServiceType() {
                            return ServiceType.ANIMATION;
                        }

                        @Override
                        public void onLowPowerModeChanged(PowerSaveState result) {
                            synchronized (mGlobalLock) {
                                final boolean enabled = result.batterySaverEnabled;
                                if (mAnimationsDisabled != enabled && !mAllowAnimationsInLowPowerMode) {
                                    mAnimationsDisabled = enabled;
                                    dispatchNewAnimatorScaleLocked(null);
                                }
                            }
                        }
                    });
            mAnimationsDisabled = mPowerManagerInternal
                    .getLowPowerState(ServiceType.ANIMATION).batterySaverEnabled;
        }
        mScreenFrozenLock = mPowerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "SCREEN_FROZEN");
        mScreenFrozenLock.setReferenceCounted(false);

        mActivityManager = ActivityManager.getService();
        mActivityTaskManager = ActivityTaskManager.getService();
        mAmInternal = LocalServices.getService(ActivityManagerInternal.class);
        mAtmInternal = LocalServices.getService(ActivityTaskManagerInternal.class);
        mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        AppOpsManager.OnOpChangedInternalListener opListener =
                new AppOpsManager.OnOpChangedInternalListener() {
                    @Override
                    public void onOpChanged(int op, String packageName) {
                        updateAppOpsState();
                    }
                };
        mAppOps.startWatchingMode(OP_SYSTEM_ALERT_WINDOW, null, opListener);
        mAppOps.startWatchingMode(AppOpsManager.OP_TOAST_WINDOW, null, opListener);

        mPmInternal = LocalServices.getService(PackageManagerInternal.class);
        final IntentFilter suspendPackagesFilter = new IntentFilter();
        suspendPackagesFilter.addAction(Intent.ACTION_PACKAGES_SUSPENDED);
        suspendPackagesFilter.addAction(Intent.ACTION_PACKAGES_UNSUSPENDED);
        context.registerReceiverAsUser(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String[] affectedPackages =
                        intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                final boolean suspended =
                        Intent.ACTION_PACKAGES_SUSPENDED.equals(intent.getAction());
                updateHiddenWhileSuspendedState(new ArraySet<>(Arrays.asList(affectedPackages)),
                        suspended);
            }
        }, UserHandle.ALL, suspendPackagesFilter, null, null);

        final ContentResolver resolver = context.getContentResolver();
        // Get persisted window scale setting
        mWindowAnimationScaleSetting = Settings.Global.getFloat(resolver,
                Settings.Global.WINDOW_ANIMATION_SCALE, mWindowAnimationScaleSetting);
        mTransitionAnimationScaleSetting = Settings.Global.getFloat(resolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                context.getResources().getFloat(
                        R.dimen.config_appTransitionAnimationDurationScaleDefault));

        setAnimatorDurationScale(Settings.Global.getFloat(resolver,
                Settings.Global.ANIMATOR_DURATION_SCALE, mAnimatorDurationScaleSetting));

        mForceDesktopModeOnExternalDisplays = Settings.Global.getInt(resolver,
                DEVELOPMENT_FORCE_DESKTOP_MODE_ON_EXTERNAL_DISPLAYS, 0) != 0;

        IntentFilter filter = new IntentFilter();
        // Track changes to DevicePolicyManager state so we can enable/disable keyguard.
        filter.addAction(ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED);
        mContext.registerReceiverAsUser(mBroadcastReceiver, UserHandle.ALL, filter, null, null);

        mLatencyTracker = LatencyTracker.getInstance(context);

        mSettingsObserver = new SettingsObserver();

        mHoldingScreenWakeLock = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG_WM);
        mHoldingScreenWakeLock.setReferenceCounted(false);

        mSurfaceAnimationRunner = new SurfaceAnimationRunner(mPowerManagerInternal);

        mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromWindowLayout);

        mTaskPositioningController = new TaskPositioningController(
                this, mInputManager, mActivityTaskManager, mH.getLooper());
        mDragDropController = new DragDropController(this, mH.getLooper());

        mHighRefreshRateBlacklist = HighRefreshRateBlacklist.create(context.getResources());

        mSystemGestureExclusionLimitDp = Math.max(MIN_GESTURE_EXCLUSION_LIMIT_DP,
                DeviceConfig.getInt(DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                        KEY_SYSTEM_GESTURE_EXCLUSION_LIMIT_DP, 0));
        mSystemGestureExclusionLogDebounceTimeoutMillis =
                DeviceConfig.getInt(DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                        KEY_SYSTEM_GESTURE_EXCLUSION_LOG_DEBOUNCE_MILLIS, 0);
        mSystemGestureExcludedByPreQStickyImmersive =
                DeviceConfig.getBoolean(DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                        KEY_SYSTEM_GESTURES_EXCLUDED_BY_PRE_Q_STICKY_IMMERSIVE, false);
        DeviceConfig.addOnPropertiesChangedListener(DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                new HandlerExecutor(mH), properties -> {
                    synchronized (mGlobalLock) {
                        final int exclusionLimitDp = Math.max(MIN_GESTURE_EXCLUSION_LIMIT_DP,
                                DeviceConfig.getInt(DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                                        KEY_SYSTEM_GESTURE_EXCLUSION_LIMIT_DP, 0));
                        final boolean excludedByPreQSticky = DeviceConfig.getBoolean(
                                DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                                KEY_SYSTEM_GESTURES_EXCLUDED_BY_PRE_Q_STICKY_IMMERSIVE, false);
                        if (mSystemGestureExcludedByPreQStickyImmersive != excludedByPreQSticky
                                || mSystemGestureExclusionLimitDp != exclusionLimitDp) {
                            mSystemGestureExclusionLimitDp = exclusionLimitDp;
                            mSystemGestureExcludedByPreQStickyImmersive = excludedByPreQSticky;
                            mRoot.forAllDisplays(DisplayContent::updateSystemGestureExclusionLimit);
                        }

                        mSystemGestureExclusionLogDebounceTimeoutMillis =
                                DeviceConfig.getInt(DeviceConfig.NAMESPACE_WINDOW_MANAGER,
                                        KEY_SYSTEM_GESTURE_EXCLUSION_LOG_DEBOUNCE_MILLIS, 0);
                    }
                });

        LocalServices.addService(WindowManagerInternal.class, new LocalService());
        //todo 여기에 맵을 정의를 해야 한다. <매우 중요>
        map = initWindwoHashMap();


    }

    private HashMap<Integer, Function<WindowToken, Integer>> initWindwoHashMap() {
        HashMap<Integer, Function<WindowToken, Integer>> map = new HashMap<>();

        Function<WindowToken, Integer> func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add application window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "Attempted to add input method window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_INPUT_METHOD, func);

        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add voice interaction window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "Attempted to add voice interaction window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_VOICE_INTERACTION, func);


        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add wallpaper window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "Attempted to add wallpaper window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_WALLPAPER, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add Dream window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_DREAM) {
                Slog.w(TAG_WM, "Attempted to add Dream window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_DREAM, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };

        map.put(TYPE_ACCESSIBILITY_OVERLAY, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                if (doesAddToastWindowRequireToken(attrs.packageName, callingUid,
                        parentWindow)) {
                    Slog.w(TAG_WM, "Attempted to add a toast window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else {
                addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName,
                        callingUid, parentWindow);
                if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                    Slog.w(TAG_WM, "Attempted to add a toast window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            }
            return null;
        };

        map.put(TYPE_TOAST, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add QS dialog window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "Attempted to add QS dialog window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };

        map.put(TYPE_QS_DIALOG, func);

        return map;
    }

    private HashMap<Integer, Function<WindowToken, Integer>> initWindowHashMapAdvance() { //todo using HashMap
        HashMap<Integer, Function<WindowToken, Integer>> map = new HashMap<>();

        Function<WindowToken, Integer> func = token -> {
            // 초기화 하는 부분
            return Optional.ofNullable(token)
                    .ifPresent(windowToken -> if());




            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add application window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "Attempted to add input method window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_INPUT_METHOD, func);

        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add voice interaction window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "Attempted to add voice interaction window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_VOICE_INTERACTION, func);


        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add wallpaper window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "Attempted to add wallpaper window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_WALLPAPER, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add Dream window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_DREAM) {
                Slog.w(TAG_WM, "Attempted to add Dream window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };
        map.put(TYPE_DREAM, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };

        map.put(TYPE_ACCESSIBILITY_OVERLAY, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                if (doesAddToastWindowRequireToken(attrs.packageName, callingUid,
                        parentWindow)) {
                    Slog.w(TAG_WM, "Attempted to add a toast window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else {
                addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName,
                        callingUid, parentWindow);
                if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                    Slog.w(TAG_WM, "Attempted to add a toast window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            }
            return null;
        };

        map.put(TYPE_TOAST, func);
        func = token -> {
            // 초기화 하는 부분
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add QS dialog window with unknown token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else if (token.windowType != TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "Attempted to add QS dialog window with bad token "
                        + attrs.token + ".  Aborting.");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            } else return null;
        };

        map.put(TYPE_QS_DIALOG, func);

        return map;
    }

    public int testWindow(Session session, IWindow client, int seq,
                          WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame,
                          Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
                          DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel,
                          InsetsState outInsetsState) {
        int[] appOp = new int[1];
        int res = mPolicy.checkAddPermission(attrs, appOp);
        if (res != WindowManagerGlobal.ADD_OKAY) {
            return res;
        }

        boolean reportNewConfig = false;
        WindowState parentWindow = null;
        long origId;
        final int callingUid = Binder.getCallingUid();
        final int type = attrs.type;

        synchronized (mGlobalLock) {
            if (!mDisplayReady) {
                throw new IllegalStateException("Display has not been initialialized");
            }

            final DisplayContent displayContent = getDisplayContentOrCreate(displayId, attrs.token);

            if (displayContent == null) {
                Slog.w(TAG_WM, "Attempted to add window to a display that does not exist: "
                        + displayId + ".  Aborting.");
                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
            }
            if (!displayContent.hasAccess(session.mUid)) {
                Slog.w(TAG_WM, "Attempted to add window to a display for which the application "
                        + "does not have access: " + displayId + ".  Aborting.");
                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
            }

            if (mWindowMap.containsKey(client.asBinder())) {
                Slog.w(TAG_WM, "Window " + client + " is already added");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;
            }

            if (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW) {
                parentWindow = windowForClientLocked(null, attrs.token, false);
                if (parentWindow == null) {
                    Slog.w(TAG_WM, "Attempted to add window with token that is not a window: "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
                }
                if (parentWindow.mAttrs.type >= FIRST_SUB_WINDOW
                        && parentWindow.mAttrs.type <= LAST_SUB_WINDOW) {
                    Slog.w(TAG_WM, "Attempted to add window with token that is a sub-window: "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
                }
            }

            if (type == TYPE_PRIVATE_PRESENTATION && !displayContent.isPrivate()) {
                Slog.w(TAG_WM, "Attempted to add private presentation window to a non-private display.  Aborting.");
                return WindowManagerGlobal.ADD_PERMISSION_DENIED;
            }

            AppWindowToken atoken = null;
            final boolean hasParent = parentWindow != null;
            // Use existing parent window token for child windows since they go in the same token
            // as there parent window so we can apply the same policy on them.
            WindowToken token = displayContent.getWindowToken(
                    hasParent ? parentWindow.mAttrs.token : attrs.token);
            // If this is a child window, we want to apply the same type checking rules as the
            // parent window type.
            final int rootType = hasParent ? parentWindow.mAttrs.type : type;

            boolean addToastWindowRequiresToken = false;
            //todo 건들이지 말기
            if (token == null) {
                if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                    Slog.w(TAG_WM, "Attempted to add application window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                Integer value = map.get(rootType).apply(token);
                if (value != null) return value;

                final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
                final boolean isRoundedCornerOverlay =
                        (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
                token = new WindowToken(this, binder, type, false, displayContent,
                        session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);
            } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                atoken = token.asAppWindowToken();
                if (atoken == null) {
                    Slog.w(TAG_WM, "Attempted to add window with non-application token "
                            + token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_NOT_APP_TOKEN;
                } else if (atoken.removed) {
                    Slog.w(TAG_WM, "Attempted to add window with exiting application token "
                            + token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_APP_EXITING;
                } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                    Slog.w(TAG_WM, "Attempted to add starting window to token with already existing"
                            + " starting window");
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD;
                }
            } else if (token.asAppWindowToken() != null) {
                Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
                // It is not valid to use an app token with other system types; we will
                // instead make a new token for it (as if null had been passed in for the token).
                attrs.token = null;
                token = new WindowToken(this, client.asBinder(), type, false, displayContent,
                        session.mCanAddInternalSystemWindow);
            } else {
                Integer value = map.get(rootType).apply(token);
                if (value != null) return value;

                value = map.get(type).apply(token);
                if (value != null) return value;
            }


            final WindowState win = new WindowState(this, session, client, token, parentWindow,
                    appOp[0], seq, attrs, viewVisibility, session.mUid,
                    session.mCanAddInternalSystemWindow);
            if (win.mDeathRecipient == null) {
                // Client has apparently died, so there is no reason to
                // continue.
                Slog.w(TAG_WM, "Adding window client " + client.asBinder()
                        + " that is dead, aborting.");
                return WindowManagerGlobal.ADD_APP_EXITING;
            }

            if (win.getDisplayContent() == null) {
                Slog.w(TAG_WM, "Adding window to Display that has been removed.");
                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
            }

            final DisplayPolicy displayPolicy = displayContent.getDisplayPolicy();
            displayPolicy.adjustWindowParamsLw(win, win.mAttrs, Binder.getCallingPid(),
                    Binder.getCallingUid());
            win.setShowToOwnerOnlyLocked(mPolicy.checkShowToOwnerOnly(attrs));

            res = displayPolicy.prepareAddWindowLw(win, attrs);
            if (res != WindowManagerGlobal.ADD_OKAY) {
                return res;
            }

            final boolean openInputChannels = (outInputChannel != null
                    && (attrs.inputFeatures & INPUT_FEATURE_NO_INPUT_CHANNEL) == 0);
            if (openInputChannels) {
                win.openInputChannel(outInputChannel);
            }

            // If adding a toast requires a token for this app we always schedule hiding
            // toast windows to make sure they don't stick around longer then necessary.
            // We hide instead of remove such windows as apps aren't prepared to handle
            // windows being removed under them.
            //
            // If the app is older it can add toasts without a token and hence overlay
            // other apps. To be maximally compatible with these apps we will hide the
            // window after the toast timeout only if the focused window is from another
            // UID, otherwise we allow unlimited duration. When a UID looses focus we
            // schedule hiding all of its toast windows.
            if (type == TYPE_TOAST) {
                if (!displayContent.canAddToastWindowForUid(callingUid)) {
                    Slog.w(TAG_WM, "Adding more than one toast window for UID at a time.");
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD;
                }
                // Make sure this happens before we moved focus as one can make the
                // toast focusable to force it not being hidden after the timeout.
                // Focusable toasts are always timed out to prevent a focused app to
                // show a focusable toasts while it has focus which will be kept on
                // the screen after the activity goes away.
                if (addToastWindowRequiresToken
                        || (attrs.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0
                        || displayContent.mCurrentFocus == null
                        || displayContent.mCurrentFocus.mOwnerUid != callingUid) {
                    mH.sendMessageDelayed(
                            mH.obtainMessage(H.WINDOW_HIDE_TIMEOUT, win),
                            win.mAttrs.hideTimeoutMilliseconds);
                }
            }

            // From now on, no exceptions or errors allowed!

            res = WindowManagerGlobal.ADD_OKAY;
            if (displayContent.mCurrentFocus == null) {
                displayContent.mWinAddedSinceNullFocus.add(win);
            }

            if (excludeWindowTypeFromTapOutTask(type)) {
                displayContent.mTapExcludedWindows.add(win);
            }

            origId = Binder.clearCallingIdentity();

            win.attach();
            mWindowMap.put(client.asBinder(), win);

            win.initAppOpsState();

            final boolean suspended = mPmInternal.isPackageSuspended(win.getOwningPackage(),
                    UserHandle.getUserId(win.getOwningUid()));
            win.setHiddenWhileSuspended(suspended);

            final boolean hideSystemAlertWindows = !mHidingNonSystemOverlayWindows.isEmpty();
            win.setForceHideNonSystemOverlayWindowIfNeeded(hideSystemAlertWindows);

            final AppWindowToken aToken = token.asAppWindowToken();
            if (type == TYPE_APPLICATION_STARTING && aToken != null) {
                aToken.startingWindow = win;
                if (DEBUG_STARTING_WINDOW) Slog.v(TAG_WM, "addWindow: " + aToken
                        + " startingWindow=" + win);
            }

            boolean imMayMove = true;

            win.mToken.addWindow(win);
            if (type == TYPE_INPUT_METHOD) {
                displayContent.setInputMethodWindowLocked(win);
                imMayMove = false;
            } else if (type == TYPE_INPUT_METHOD_DIALOG) {
                displayContent.computeImeTarget(true /* updateImeTarget */);
                imMayMove = false;
            } else {
                if (type == TYPE_WALLPAPER) {
                    displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                } else if ((attrs.flags & FLAG_SHOW_WALLPAPER) != 0) {
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
                    // If there is currently a wallpaper being shown, and
                    // the base layer of the new window is below the current
                    // layer of the target window, then adjust the wallpaper.
                    // This is to avoid a new window being placed between the
                    // wallpaper and its target.
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                }
            }

            // If the window is being added to a stack that's currently adjusted for IME,
            // make sure to apply the same adjust to this new window.
            win.applyAdjustForImeIfNeeded();

            if (type == TYPE_DOCK_DIVIDER) {
                mRoot.getDisplayContent(displayId).getDockedDividerController().setWindow(win);
            }

            final WindowStateAnimator winAnimator = win.mWinAnimator;
            winAnimator.mEnterAnimationPending = true;
            winAnimator.mEnteringAnimation = true;
            // Check if we need to prepare a transition for replacing window first.
            if (atoken != null && atoken.isVisible()
                    && !prepareWindowReplacementTransition(atoken)) {
                // If not, check if need to set up a dummy transition during display freeze
                // so that the unfreeze wait for the apps to draw. This might be needed if
                // the app is relaunching.
                prepareNoneTransitionForRelaunching(atoken);
            }

            final DisplayFrames displayFrames = displayContent.mDisplayFrames;
            // TODO: Not sure if onDisplayInfoUpdated() call is needed.
            final DisplayInfo displayInfo = displayContent.getDisplayInfo();
            displayFrames.onDisplayInfoUpdated(displayInfo,
                    displayContent.calculateDisplayCutoutForRotation(displayInfo.rotation));
            final Rect taskBounds;
            final boolean floatingStack;
            if (atoken != null && atoken.getTask() != null) {
                taskBounds = mTmpRect;
                atoken.getTask().getBounds(mTmpRect);
                floatingStack = atoken.getTask().isFloating();
            } else {
                taskBounds = null;
                floatingStack = false;
            }
            if (displayPolicy.getLayoutHintLw(win.mAttrs, taskBounds, displayFrames, floatingStack,
                    outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout)) {
                res |= WindowManagerGlobal.ADD_FLAG_ALWAYS_CONSUME_SYSTEM_BARS;
            }
            outInsetsState.set(displayContent.getInsetsStateController().getInsetsForDispatch(win));

            if (mInTouchMode) {
                res |= WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE;
            }
            if (win.mAppToken == null || !win.mAppToken.isClientHidden()) {
                res |= WindowManagerGlobal.ADD_FLAG_APP_VISIBLE;
            }

            displayContent.getInputMonitor().setUpdateInputWindowsNeededLw();

            boolean focusChanged = false;
            if (win.canReceiveKeys()) {
                focusChanged = updateFocusedWindowLocked(UPDATE_FOCUS_WILL_ASSIGN_LAYERS,
                        false /*updateInputWindows*/);
                if (focusChanged) {
                    imMayMove = false;
                }
            }

            if (imMayMove) {
                displayContent.computeImeTarget(true /* updateImeTarget */);
            }

            // Don't do layout here, the window must call
            // relayout to be displayed, so we'll do it there.
            win.getParent().assignChildLayers();

            if (focusChanged) {
                displayContent.getInputMonitor().setInputFocusLw(displayContent.mCurrentFocus,
                        false /*updateInputWindows*/);
            }
            displayContent.getInputMonitor().updateInputWindowsLw(false /*force*/);

            if (localLOGV || DEBUG_ADD_REMOVE) Slog.v(TAG_WM, "addWindow: New client "
                    + client.asBinder() + ": window=" + win + " Callers=" + Debug.getCallers(5));

            if (win.isVisibleOrAdding() && displayContent.updateOrientationFromAppTokens()) {
                reportNewConfig = true;
            }
        }

        if (reportNewConfig) {
            sendNewConfiguration(displayId);
        }

        Binder.restoreCallingIdentity(origId);

        return res;
    }

    public int addWindow(Session session, IWindow client, int seq,
                         WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame,
                         Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
                         DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel,
                         InsetsState outInsetsState) {
        int[] appOp = new int[1];
        int res = mPolicy.checkAddPermission(attrs, appOp);
        if (res != WindowManagerGlobal.ADD_OKAY) {
            return res;
        }

        boolean reportNewConfig = false;
        WindowState parentWindow = null;
        long origId;
        final int callingUid = Binder.getCallingUid();
        final int type = attrs.type;

        synchronized (mGlobalLock) {
            if (!mDisplayReady) {
                throw new IllegalStateException("Display has not been initialialized");
            }

            final DisplayContent displayContent = getDisplayContentOrCreate(displayId, attrs.token);

            if (displayContent == null) {
                Slog.w(TAG_WM, "Attempted to add window to a display that does not exist: "
                        + displayId + ".  Aborting.");
                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
            }
            if (!displayContent.hasAccess(session.mUid)) {
                Slog.w(TAG_WM, "Attempted to add window to a display for which the application "
                        + "does not have access: " + displayId + ".  Aborting.");
                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
            }

            if (mWindowMap.containsKey(client.asBinder())) {
                Slog.w(TAG_WM, "Window " + client + " is already added");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;
            }

            if (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW) {
                parentWindow = windowForClientLocked(null, attrs.token, false);
                if (parentWindow == null) {
                    Slog.w(TAG_WM, "Attempted to add window with token that is not a window: "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
                }
                if (parentWindow.mAttrs.type >= FIRST_SUB_WINDOW
                        && parentWindow.mAttrs.type <= LAST_SUB_WINDOW) {
                    Slog.w(TAG_WM, "Attempted to add window with token that is a sub-window: "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
                }
            }

            if (type == TYPE_PRIVATE_PRESENTATION && !displayContent.isPrivate()) {
                Slog.w(TAG_WM, "Attempted to add private presentation window to a non-private display.  Aborting.");
                return WindowManagerGlobal.ADD_PERMISSION_DENIED;
            }

            AppWindowToken atoken = null;
            final boolean hasParent = parentWindow != null;
            // Use existing parent window token for child windows since they go in the same token
            // as there parent window so we can apply the same policy on them.
            WindowToken token = displayContent.getWindowToken(
                    hasParent ? parentWindow.mAttrs.token : attrs.token);
            // If this is a child window, we want to apply the same type checking rules as the
            // parent window type.
            final int rootType = hasParent ? parentWindow.mAttrs.type : type;

            boolean addToastWindowRequiresToken = false;
            //todo 건들이지 말기
            if (token == null) {
                if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                    Slog.w(TAG_WM, "Attempted to add application window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (rootType == TYPE_INPUT_METHOD) {
                    Slog.w(TAG_WM, "Attempted to add input method window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (rootType == TYPE_VOICE_INTERACTION) {
                    Slog.w(TAG_WM, "Attempted to add voice interaction window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (rootType == TYPE_WALLPAPER) {
                    Slog.w(TAG_WM, "Attempted to add wallpaper window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (rootType == TYPE_DREAM) {
                    Slog.w(TAG_WM, "Attempted to add Dream window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (rootType == TYPE_QS_DIALOG) {
                    Slog.w(TAG_WM, "Attempted to add QS dialog window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (rootType == TYPE_ACCESSIBILITY_OVERLAY) {
                    Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with unknown token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
                if (type == TYPE_TOAST) {
                    // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                    if (doesAddToastWindowRequireToken(attrs.packageName, callingUid,
                            parentWindow)) {
                        Slog.w(TAG_WM, "Attempted to add a toast window with unknown token "
                                + attrs.token + ".  Aborting.");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                }
                final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
                final boolean isRoundedCornerOverlay =
                        (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
                token = new WindowToken(this, binder, type, false, displayContent,
                        session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);
            } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                atoken = token.asAppWindowToken();
                if (atoken == null) {
                    Slog.w(TAG_WM, "Attempted to add window with non-application token "
                            + token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_NOT_APP_TOKEN;
                } else if (atoken.removed) {
                    Slog.w(TAG_WM, "Attempted to add window with exiting application token "
                            + token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_APP_EXITING;
                } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                    Slog.w(TAG_WM, "Attempted to add starting window to token with already existing"
                            + " starting window");
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD;
                }
            } else if (rootType == TYPE_INPUT_METHOD) {
                if (token.windowType != TYPE_INPUT_METHOD) {
                    Slog.w(TAG_WM, "Attempted to add input method window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_VOICE_INTERACTION) {
                if (token.windowType != TYPE_VOICE_INTERACTION) {
                    Slog.w(TAG_WM, "Attempted to add voice interaction window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_WALLPAPER) {
                if (token.windowType != TYPE_WALLPAPER) {
                    Slog.w(TAG_WM, "Attempted to add wallpaper window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_DREAM) {
                if (token.windowType != TYPE_DREAM) {
                    Slog.w(TAG_WM, "Attempted to add Dream window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_ACCESSIBILITY_OVERLAY) {
                if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                    Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (type == TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName,
                        callingUid, parentWindow);
                if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                    Slog.w(TAG_WM, "Attempted to add a toast window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (type == TYPE_QS_DIALOG) {
                if (token.windowType != TYPE_QS_DIALOG) {
                    Slog.w(TAG_WM, "Attempted to add QS dialog window with bad token "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (token.asAppWindowToken() != null) {
                Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
                // It is not valid to use an app token with other system types; we will
                // instead make a new token for it (as if null had been passed in for the token).
                attrs.token = null;
                token = new WindowToken(this, client.asBinder(), type, false, displayContent,
                        session.mCanAddInternalSystemWindow);
            }

            final WindowState win = new WindowState(this, session, client, token, parentWindow,
                    appOp[0], seq, attrs, viewVisibility, session.mUid,
                    session.mCanAddInternalSystemWindow);
            if (win.mDeathRecipient == null) {
                // Client has apparently died, so there is no reason to
                // continue.
                Slog.w(TAG_WM, "Adding window client " + client.asBinder()
                        + " that is dead, aborting.");
                return WindowManagerGlobal.ADD_APP_EXITING;
            }

            if (win.getDisplayContent() == null) {
                Slog.w(TAG_WM, "Adding window to Display that has been removed.");
                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
            }

            final DisplayPolicy displayPolicy = displayContent.getDisplayPolicy();
            displayPolicy.adjustWindowParamsLw(win, win.mAttrs, Binder.getCallingPid(),
                    Binder.getCallingUid());
            win.setShowToOwnerOnlyLocked(mPolicy.checkShowToOwnerOnly(attrs));

            res = displayPolicy.prepareAddWindowLw(win, attrs);
            if (res != WindowManagerGlobal.ADD_OKAY) {
                return res;
            }

            final boolean openInputChannels = (outInputChannel != null
                    && (attrs.inputFeatures & INPUT_FEATURE_NO_INPUT_CHANNEL) == 0);
            if (openInputChannels) {
                win.openInputChannel(outInputChannel);
            }

            // If adding a toast requires a token for this app we always schedule hiding
            // toast windows to make sure they don't stick around longer then necessary.
            // We hide instead of remove such windows as apps aren't prepared to handle
            // windows being removed under them.
            //
            // If the app is older it can add toasts without a token and hence overlay
            // other apps. To be maximally compatible with these apps we will hide the
            // window after the toast timeout only if the focused window is from another
            // UID, otherwise we allow unlimited duration. When a UID looses focus we
            // schedule hiding all of its toast windows.
            if (type == TYPE_TOAST) {
                if (!displayContent.canAddToastWindowForUid(callingUid)) {
                    Slog.w(TAG_WM, "Adding more than one toast window for UID at a time.");
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD;
                }
                // Make sure this happens before we moved focus as one can make the
                // toast focusable to force it not being hidden after the timeout.
                // Focusable toasts are always timed out to prevent a focused app to
                // show a focusable toasts while it has focus which will be kept on
                // the screen after the activity goes away.
                if (addToastWindowRequiresToken
                        || (attrs.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0
                        || displayContent.mCurrentFocus == null
                        || displayContent.mCurrentFocus.mOwnerUid != callingUid) {
                    mH.sendMessageDelayed(
                            mH.obtainMessage(H.WINDOW_HIDE_TIMEOUT, win),
                            win.mAttrs.hideTimeoutMilliseconds);
                }
            }

            // From now on, no exceptions or errors allowed!

            res = WindowManagerGlobal.ADD_OKAY;
            if (displayContent.mCurrentFocus == null) {
                displayContent.mWinAddedSinceNullFocus.add(win);
            }

            if (excludeWindowTypeFromTapOutTask(type)) {
                displayContent.mTapExcludedWindows.add(win);
            }

            origId = Binder.clearCallingIdentity();

            win.attach();
            mWindowMap.put(client.asBinder(), win);

            win.initAppOpsState();

            final boolean suspended = mPmInternal.isPackageSuspended(win.getOwningPackage(),
                    UserHandle.getUserId(win.getOwningUid()));
            win.setHiddenWhileSuspended(suspended);

            final boolean hideSystemAlertWindows = !mHidingNonSystemOverlayWindows.isEmpty();
            win.setForceHideNonSystemOverlayWindowIfNeeded(hideSystemAlertWindows);

            final AppWindowToken aToken = token.asAppWindowToken();
            if (type == TYPE_APPLICATION_STARTING && aToken != null) {
                aToken.startingWindow = win;
                if (DEBUG_STARTING_WINDOW) Slog.v(TAG_WM, "addWindow: " + aToken
                        + " startingWindow=" + win);
            }

            boolean imMayMove = true;

            win.mToken.addWindow(win);
            if (type == TYPE_INPUT_METHOD) {
                displayContent.setInputMethodWindowLocked(win);
                imMayMove = false;
            } else if (type == TYPE_INPUT_METHOD_DIALOG) {
                displayContent.computeImeTarget(true /* updateImeTarget */);
                imMayMove = false;
            } else {
                if (type == TYPE_WALLPAPER) {
                    displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                } else if ((attrs.flags & FLAG_SHOW_WALLPAPER) != 0) {
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
                    // If there is currently a wallpaper being shown, and
                    // the base layer of the new window is below the current
                    // layer of the target window, then adjust the wallpaper.
                    // This is to avoid a new window being placed between the
                    // wallpaper and its target.
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                }
            }

            // If the window is being added to a stack that's currently adjusted for IME,
            // make sure to apply the same adjust to this new window.
            win.applyAdjustForImeIfNeeded();

            if (type == TYPE_DOCK_DIVIDER) {
                mRoot.getDisplayContent(displayId).getDockedDividerController().setWindow(win);
            }

            final WindowStateAnimator winAnimator = win.mWinAnimator;
            winAnimator.mEnterAnimationPending = true;
            winAnimator.mEnteringAnimation = true;
            // Check if we need to prepare a transition for replacing window first.
            if (atoken != null && atoken.isVisible()
                    && !prepareWindowReplacementTransition(atoken)) {
                // If not, check if need to set up a dummy transition during display freeze
                // so that the unfreeze wait for the apps to draw. This might be needed if
                // the app is relaunching.
                prepareNoneTransitionForRelaunching(atoken);
            }

            final DisplayFrames displayFrames = displayContent.mDisplayFrames;
            // TODO: Not sure if onDisplayInfoUpdated() call is needed.
            final DisplayInfo displayInfo = displayContent.getDisplayInfo();
            displayFrames.onDisplayInfoUpdated(displayInfo,
                    displayContent.calculateDisplayCutoutForRotation(displayInfo.rotation));
            final Rect taskBounds;
            final boolean floatingStack;
            if (atoken != null && atoken.getTask() != null) {
                taskBounds = mTmpRect;
                atoken.getTask().getBounds(mTmpRect);
                floatingStack = atoken.getTask().isFloating();
            } else {
                taskBounds = null;
                floatingStack = false;
            }
            if (displayPolicy.getLayoutHintLw(win.mAttrs, taskBounds, displayFrames, floatingStack,
                    outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout)) {
                res |= WindowManagerGlobal.ADD_FLAG_ALWAYS_CONSUME_SYSTEM_BARS;
            }
            outInsetsState.set(displayContent.getInsetsStateController().getInsetsForDispatch(win));

            if (mInTouchMode) {
                res |= WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE;
            }
            if (win.mAppToken == null || !win.mAppToken.isClientHidden()) {
                res |= WindowManagerGlobal.ADD_FLAG_APP_VISIBLE;
            }

            displayContent.getInputMonitor().setUpdateInputWindowsNeededLw();

            boolean focusChanged = false;
            if (win.canReceiveKeys()) {
                focusChanged = updateFocusedWindowLocked(UPDATE_FOCUS_WILL_ASSIGN_LAYERS,
                        false /*updateInputWindows*/);
                if (focusChanged) {
                    imMayMove = false;
                }
            }

            if (imMayMove) {
                displayContent.computeImeTarget(true /* updateImeTarget */);
            }

            // Don't do layout here, the window must call
            // relayout to be displayed, so we'll do it there.
            win.getParent().assignChildLayers();

            if (focusChanged) {
                displayContent.getInputMonitor().setInputFocusLw(displayContent.mCurrentFocus,
                        false /*updateInputWindows*/);
            }
            displayContent.getInputMonitor().updateInputWindowsLw(false /*force*/);

            if (localLOGV || DEBUG_ADD_REMOVE) Slog.v(TAG_WM, "addWindow: New client "
                    + client.asBinder() + ": window=" + win + " Callers=" + Debug.getCallers(5));

            if (win.isVisibleOrAdding() && displayContent.updateOrientationFromAppTokens()) {
                reportNewConfig = true;
            }
        }

        if (reportNewConfig) {
            sendNewConfiguration(displayId);
        }

        Binder.restoreCallingIdentity(origId);

        return res;
    }
}
