package com.keelim.test.after;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Mapping { // Stream 을 활용한 정리를 할 것
    int rootType = 0;

    Map<Integer, Function<String, Integer>> map = new HashMap<>();

    public Map<Integer, Function<String, Integer>> getMap() {
        return map;
    }

    public void init() {
        Function<String, Integer> func = (s) -> {
            if (s == null) {
                System.out.println(s+"안녕");
            } else {
                System.out.println(s+"잘가");
            }
            return -1;
        };

        for (int i = 0; i < 5; i++) {
            map.put(i, func);
        }
    }
}