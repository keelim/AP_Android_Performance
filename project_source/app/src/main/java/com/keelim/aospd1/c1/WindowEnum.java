package com.keelim.aospd1.c1;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;

public enum WindowEnum {
    // Enum 을 굳이 쓸수가 있나? -> 어차피 변환을 해야 하는 상황인 것 같다.
    // 일단 비교하는 값은 root type 이라 확정을 짓는 것은 조금 어려운 것 같다.
    // 일단 두개 테스트를 해보는 수 밖에 없을 것 같다.

    FIRST_APPLICATION_WINDOW(1),
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

    }
}

