package com.keelim.test.unit1;

import com.keelim.test.after.WindowManagerGlobal;
import com.keelim.test.after.WindowToken;

import java.util.HashMap;
import java.util.function.Function;

import static android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_DREAM;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_QS_DIALOG;
import static com.keelim.test.after.WindowManager.LayoutParams.TYPE_VOICE_INTERACTION;

public class Map {
    private final HashMap<Integer, Function<WindowToken, Integer>> map;

    public Map() {
        map = new HashMap<>();
        Function<WindowToken, Integer> func = (to) -> {
            if (to == null) {
                System.out.println("s1");
            } else if (to.windowType != TYPE_INPUT_METHOD) {
                System.out.println("s8");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };
        map.put(TYPE_INPUT_METHOD, func);

        func = (to) -> {
            if (to == null) {
                System.out.println("S3");
            } else if (to.windowType != TYPE_VOICE_INTERACTION) {
                System.out.println("s9");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };
        map.put(TYPE_VOICE_INTERACTION, func);

        func = (to) -> {
            if (to == null) {
                System.out.println("s4");
            } else if (to.windowType != TYPE_WALLPAPER) {
                System.out.println("s10");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_WALLPAPER, func);

        func = (to) -> {
            if (to == null) {
                System.out.println("s5");
            } else if (to.windowType != TYPE_DREAM) {
                System.out.println("s11");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_WALLPAPER, func);

        func = (to) -> {
            if (to == null) {
                System.out.println("s6");
            } else if (to.windowType != TYPE_QS_DIALOG) {
                System.out.println("s14");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_QS_DIALOG, func);


        func = (to) -> {
            if (to == null) {
                System.out.println("s7");
            } else if (to.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                System.out.println("s12");
            }
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        };

        map.put(TYPE_ACCESSIBILITY_OVERLAY, func); // 초기화 완료
    }

    private int service(int rootType){
        map.get(rootType)
    }
}
