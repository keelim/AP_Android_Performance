package com.keelim.aospd1.c2;

import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class Controller1 {
    public static final int BAD_TOKEN = -1;
    private final HashMap<Integer, Function<Object, Integer>> hashMap;
    private final Integer[] key = {1, 2, 3, 4, 5};
    private final Random random;


    public Controller1() {
        random = new Random();

        Function<Object, Integer> func = obj -> {
            if (obj == null) System.out.println("First outcome");
            else System.out.println("Second outcome");
            return BAD_TOKEN;
        };

        hashMap = new HashMap<Integer, Function<Object, Integer>>() {{
            for (int i = 0; i < 5; i++) {
                put(key[i], func);
            }
        }}; // 일단 초기화 완료
    }

    public int run() {
        int key = random.nextInt(6); //0, 1, 2, 3, 4
        int ob = random.nextInt(2); //0, 1
        Object objects = (ob == 0) ? new Object() : null;

        return Optional.ofNullable(hashMap.get(key))
                .map(map->map.apply(objects))
                .orElse(1);
    }
}
