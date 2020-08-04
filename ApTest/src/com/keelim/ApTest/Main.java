package com.keelim.ApTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static Controller0 controller0;
    private static Controller1 controller1;

    private static BufferedWriter writer;


    public static void main(String[] args) {

        try {
            writer = new BufferedWriter(new FileWriter(new File("performance.csv")));
            writer.write("if-else,");
            writer.write("hashMap");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        long start, finish, performance;
        controller0 = new Controller0();
        controller1 = new Controller1();


        for (int i = 0; i < 100000; i++) {
            start = System.nanoTime();
            controller0.run();
            finish = System.nanoTime();
            performance = finish - start;
            writeTime(performance);

            start = System.nanoTime();
            controller1.run();
            finish = System.nanoTime();
            performance = finish - start;
            writeTime2(performance);
        }

/*
        for (int i = 0; i < 1000; i++) {
            start = System.currentTimeMillis();
            controller2 = new Controller2();
            controller2.run();
            finish = System.currentTimeMillis();
            performance = finish - start;
            writeTime(performance, i);
        }
*/


    }

    private static void writeTime(long performance_time) {
        try {
            writer.write(String.valueOf(performance_time));
            writer.write(",");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void writeTime2(long performance_time) {
        try {
            writer.write(String.valueOf(performance_time));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

}
