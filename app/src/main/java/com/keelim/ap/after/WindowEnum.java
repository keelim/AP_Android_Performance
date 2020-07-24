package com.keelim.ap.after;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;

public enum WindowEnum { // 사용하려는 ENUM 값을 상수로 받는다?

    FIRST_APPLICATION_WINDOW(1), //static final을 활용을 하는 것일 값 자체는 변하지 않을 것 같다.
    LAST_APPLICATION_WINDOW(99),
    TYPE_TOAST(2005),
    TYPE_INPUT_METHOD(2011),
    TYPE_WALLPAPER(2013),
    TYPE_DREAM(2023),
    TYPE_VOICE_INTERACTION(2031),
    TYPE_ACCESSIBILITY_OVERLAY(2032),
    TYPE_QS_DIALOG(2035);

    int value;

    WindowEnum(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public static void main(String[] args) {
        Map<WindowEnum, Callable<Integer>> map = new EnumMap<>(WindowEnum.class);

        //스트림을 활요한 람다형 퍼포 먼스 등록1
        //Callable<>
        //Function<>
        //Binary
    }
}

