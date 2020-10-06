package patternTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class PatternTest {
    private static BufferedWriter writer;
    private static Scanner sc;

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        int flag = sc.nextInt();
        try {
            if (flag == 1) {
                writer = new BufferedWriter(new FileWriter(new File("telescoping.xlsx")));
                writer.write("telescoping");
                writer.newLine();

                System.out.println("This is Telescoping Pattern");
            } else if (flag == 2) {
                writer = new BufferedWriter(new FileWriter(new File("builder.xlsx")));
                writer.write("builder");
                writer.newLine();

                System.out.println("This is Builder Pattern");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        long s1;
        long e1;
        long[] testData = new long[1000000];

        if (flag == 1) {
            for (int i = 0; i < testData.length; i++) {
                s1 = System.nanoTime();

                LayoutParams l1 = new LayoutParams();
                LayoutParams l2 = new LayoutParams(2);
                LayoutParams l3 = new LayoutParams(1, 2);
                LayoutParams l4 = new LayoutParams(1, 2, 3);
                LayoutParams l5 = new LayoutParams(1, 2, 3, 4, 5);
                LayoutParams l6 = new LayoutParams(1, 2, 3, 4, 5, 6, 7);

                e1 = System.nanoTime();
                long a = e1 - s1;
                testData[i] = a;
                try {
                    writer.write(a + "");
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.print(testData[i] + " ");
            }
        } else if (flag == 2) {
            for (int i = 0; i < testData.length; i++) {
                s1 = System.nanoTime();

                LayoutParams2 l1 = new LayoutParams2.Builder(1).build();
                LayoutParams2 l2 = new LayoutParams2.Builder(1).
                        flags(2)
                        .build();

                LayoutParams2 l3 = new LayoutParams2.Builder(1)
                        .flags(2)
                        .format(3)
                        .build();

                LayoutParams2 l4 = new LayoutParams2.Builder(1)
                        .flags(2)
                        .format(3)
                        .wh(4, 5)
                        .build();

                LayoutParams2 l5 = new LayoutParams2.Builder(1)
                        .flags(2)
                        .format(3)
                        .wh(4, 5)
                        .xy(6, 7)
                        .build();


                e1 = System.nanoTime();
                long a = e1 - s1;
                testData[i] = a;
            }
        }

        if (testData[0] == 0) {
            System.out.println("System is error: please restart the program");
        } else {
            makeTestFile(testData);
        }
    }

    private static void makeTestFile(long[] testfile) {
        for (long l : testfile) {
            try {
                writer.write(l + "");
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}