package mathTest;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MathTest {
    private static BufferedWriter writer;
    private static Scanner sc;

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        System.out.print("Please input keyword only math: '1', only 3 statement '2'");
        int flag = sc.nextInt();

        try {
            if (flag == 1) {
                writer = new BufferedWriter(new FileWriter(new File("onlymath.xlsx")));
                writer.write("MathTest1");
                writer.newLine();
            } else if (flag == 2) {
                writer = new BufferedWriter(new FileWriter(new File("only3state.xlsx")));
                writer.write("MathTest2");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        long[] testData = new long[0];
        if (flag == 1) {
            System.out.println("Math 1 Test");
            MathTemp1 t1 = new MathTemp1();
            testData = perfom1(t1);
        } else if (flag == 2) {
            System.out.println("Math 2 Test");
            MathTemp2 t2 = new MathTemp2();
            testData = perfom2(t2);
        }

        if (testData.length == 0) {
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

    @NotNull
    private static long[] perfom1(MathTemp1 m1) {
        long[] value = new long[100000];

        for (int j = 0; j < value.length; j++) {
            long s1 = System.nanoTime();
            m1.temp1(11, 22, 33, 44);
            m1.temp2();
            long e1 = System.nanoTime();
            value[j] = e1 - s1;
            System.out.printf("%d ", value[j]);
        }

        return value;
    }

    private static long[] perfom2(MathTemp2 m2) {
        long[] value = new long[100000];

        for (int j = 0; j < value.length; j++) {
            long s1 = System.nanoTime();
            m2.temp1(11, 22, 33, 44);
            m2.temp2();
            long e1 = System.nanoTime();
            value[j] = e1 - s1;
            System.out.printf("%d ", value[j]);
        }

        return value;
    }
}
