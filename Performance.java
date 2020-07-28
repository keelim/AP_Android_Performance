package com.keelim.ap.after;

import android.os.IBinder;
import android.view.WindowManager;

import com.keelim.ap.*;
import com.keelim.test.after.WindowManagerGlobal;
import com.keelim.test.after.WindowState;
import com.keelim.test.after.WindowToken;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static android.view.WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW;
import static android.view.WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
import static android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_DREAM;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_QS_DIALOG;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_VOICE_INTERACTION;


public class Performance {
    //일단 Hash 맵으로 구현을 해보자 -> Enum 맵은 직접적으로 구현을 해야 하기 때문에 힘들 수 있다.

    //todo this comment change string block for test
    // - /*## <".  Aborting."> ->s0*/
    // - /*## <"Attempted to add application window with unknown token "> ->s1*/
    // - /*## <"Attempted to add input method window with unknown token "> ->s2*/
    // - /*##<"Attempted to add voice interaction window with unknown token "> ->s3*/
    // - /*##<"Attempted to add wallpaper window with unknown token "> ->s4*/
    // - /*##<"Attempted to add Dream window with unknown token "> ->s5*/
    // - /*##<"Attempted to add QS dialog window with unknown token "> ->s6*/
    // - /*##<"Attempted to add Accessibility overlay window with unknown token "> ->s7*/
    // - /*##<"Attempted to add input method window with bad token "> -> s8*/
    // - /*## <"Attempted to add voice interaction window with bad token "> -> s9*/
    // - /*## <"Attempted to add wallpaper window with bad token "> -> s10*/
    // - /*## <"Attempted to add Dream window with bad token "> -> s11*/
    // - /*## <"Attempted to add Accessibility overlay window with bad token "> -> s12*/
    // - /*## <"Attempted to add a toast window with bad token "> -> s13*/
    // - /*## <"Attempted to add QS dialog window with bad token "> -> s14*/
    // -/*## <"Attempted to add a toast window with unknown token "> -> s15*/s
    private Map<Integer, Function<WindowToken, Integer>> map;

    private void mapinit() {
        map = new HashMap<>();
        Function<WindowToken, Integer> func = (to) -> {
            if (to == null) {
                Slog.w(TAG_WM, "s1" + attrs.token + "s0");
            } else if (to.windowType != TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "s8" + attrs.token + "s0");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };
        map.put(TYPE_INPUT_METHOD, func);

        func = (to) -> {
            if (to == null) {
                Slog.w(TAG_WM, "s3" + attrs.token + "s0");
            } else if (to.windowType != TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "s9" + attrs.token + "s0");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };
        map.put(TYPE_VOICE_INTERACTION, func);

        func = (to) -> {
            if (to == null) {
                Slog.w(TAG_WM, "s4" + attrs.token + "s0");
            } else if (to.windowType != TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "s10" + attrs.token + "s0");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_WALLPAPER, func);

        func = (to) -> {
            if (to == null) {
                Slog.w(TAG_WM, "s5" + attrs.token + "s0");
            } else if (to.windowType != TYPE_DREAM) {
                Slog.w(TAG_WM, "s11" + attrs.token + "s0");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_WALLPAPER, func);

        func = (to) -> {
            if (to == null) {
                Slog.w(TAG_WM, "s6" + attrs.token + "s0");
            } else if (to.windowType != TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "s14" + attrs.token + "s0");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_QS_DIALOG, func);


        func = (to) -> {
            if (to == null) {
                Slog.w(TAG_WM, "s7" + attrs.token + "s0");
            } else if (to.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "s12" + attrs.token + "s0");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_ACCESSIBILITY_OVERLAY, func); // 초기화 완료
    }

    public int test5() { // mapTest
        final String TAG_WM = "WindowManager";
        final WindowState parentWindow;
        final DisplayContent displayContent;
        final WindowManager.LayoutParams attrs;
        final int type = attrs.type;
        final WindowToken atoken;
        final boolean hasParent = parentWindow != null;
        final int rootType = hasParent ? parentWindow.mAttrs.type : type;
        final WindowToken token = displayContent.getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);

        if (token == null) {
            if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                Slog.w(TAG_WM, "s1" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            //todo 건들지 않기////////////////////////////////////////////////////////////////////////////
            Integer answer = map.get(rootType).apply(token);
            if (answer != null) return answer;

            //todo 건들지 않기///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (type == TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow)) {
                    Slog.w(TAG_WM, "s15" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            }

            final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
            final boolean isRoundedCornerOverlay = (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
            token = new WindowToken(this, binder, type, false, displayContent, session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);

        } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
            atoken = token.asAppWindowToken();
            if (atoken == null) {
                Slog.w(TAG_WM, "Attempted to add window with non-application token " + token + "s0");
                return WindowManagerGlobal.ADD_NOT_APP_TOKEN;

            } else if (atoken.removed) {
                Slog.w(TAG_WM, "Attempted to add window with exiting application token " + token + "s0");
                return WindowManagerGlobal.ADD_APP_EXITING;

            } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                Slog.w(TAG_WM, "Attempted to add starting window to token with already existing" + " starting window");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;
            }


        } else {
            Integer value = map.get(rootType).apply(token);
            if (value != null) return value;

            value = map.get(type).apply(token);
            if (value != null) return value;

            if (type == TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow);
                if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                    Slog.w(TAG_WM, "s13" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }

            } else if (token.asAppWindowToken() != null) {
                Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
                // It is not valid to use an app token with other system types; we will
                // instead make a new token for it (as if null had been passed in for the token).
                attrs.token = null;
                token = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);
            }
        }

        return 0;
    }


    public int test5() { // EnumMap @AfterEnum
        final String TAG_WM = "WindowManager";
        final WindowState parentWindow;
        final DisplayContent displayContent;
        final WindowManager.LayoutParams attrs;
        final int type = attrs.type;
        final WindowToken atoken;
        final boolean hasParent = parentWindow != null;
        final int rootType = hasParent ? parentWindow.mAttrs.type : type;
        final WindowToken token = displayContent.getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);

        if (token == null) {
            if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                Slog.w(TAG_WM, "s1" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            //todo 건들지 않기////////////////////////////////////////////////////////////////////////////
            if (rootType == TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "s2" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "s3" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "s4" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_DREAM) {
                Slog.w(TAG_WM, "s5" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "s6" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "s7" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }

//todo 건들지 않기///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (type == TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow)) {
                    Slog.w(TAG_WM, "s15" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            }

            final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
            final boolean isRoundedCornerOverlay = (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
            token = new WindowToken(this, binder, type, false, displayContent, session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);

        } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
            atoken = token.asAppWindowToken();
            if (atoken == null) {
                Slog.w(TAG_WM, "Attempted to add window with non-application token " + token + "s0");
                return WindowManagerGlobal.ADD_NOT_APP_TOKEN;

            } else if (atoken.removed) {
                Slog.w(TAG_WM, "Attempted to add window with exiting application token " + token + "s0");
                return WindowManagerGlobal.ADD_APP_EXITING;

            } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                Slog.w(TAG_WM, "Attempted to add starting window to token with already existing" + " starting window");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;
            }
//todo 건들지 않기///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        } else {
            if (rootType == TYPE_INPUT_METHOD) {
                if (token.windowType != TYPE_INPUT_METHOD) {
                    Slog.w(TAG_WM, "s8" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_VOICE_INTERACTION) {
                if (token.windowType != TYPE_VOICE_INTERACTION) {
                    Slog.w(TAG_WM, "s9" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_WALLPAPER) {
                if (token.windowType != TYPE_WALLPAPER) {
                    Slog.w(TAG_WM, "s10" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_DREAM) {
                if (token.windowType != TYPE_DREAM) {
                    Slog.w(TAG_WM, "s11" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            } else if (rootType == TYPE_ACCESSIBILITY_OVERLAY) {
                if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                    Slog.w(TAG_WM, "s12" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
//todo 건들지 않기///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }

            if (type == TYPE_QS_DIALOG) { //todo 잘 확인을 할 것
                if (token.windowType != TYPE_QS_DIALOG) {
                    Slog.w(TAG_WM, "s14" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            }
            if (type == TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow);
                if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                    Slog.w(TAG_WM, "s13" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }

            } else if (token.asAppWindowToken() != null) {
                Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
                // It is not valid to use an app token with other system types; we will
                // instead make a new token for it (as if null had been passed in for the token).
                attrs.token = null;
                token = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);
            }
        }

        return 0;
    }
}