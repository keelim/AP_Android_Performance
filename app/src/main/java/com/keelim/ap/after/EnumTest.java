package com.keelim.ap.after;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EnumTest { // Stream 을 활용한 정리를 할 것
    int rootType = 0;

    Map<Integer, Function<String, Integer>> map = new HashMap<>();

    public Map<Integer, Function<String, Integer>> getMap() {
        return map;
    }

    public void init() {
        Function<String, Integer> func = (s) -> {
            if (s == null) {
                System.out.println("이것은 null 것이다.");
            } else {
                System.out.println(s+"이것은 null 이 아닌 것이다.");
            }
            return -1;
        };

        for (int i = 0; i < 5; i++) {
            map.put(i, func);
        }
    }


    public static void main(String[] args) {
        EnumTest enumTest = new EnumTest();
        enumTest.init();

        Map<Integer, Function<String, Integer>> map = enumTest.getMap();


        int a = map.get(2).apply("hello it`s me");
        System.out.println("받은 값은"+a);


    }


}
