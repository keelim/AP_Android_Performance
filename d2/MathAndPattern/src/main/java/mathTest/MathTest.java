package mathTest;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MathTest {
    private static BufferedWriter writer;

    public static void main(String[] args) {

        try {
            writer = new BufferedWriter(new FileWriter(new File("1.csv")));
            writer.write("MathTest1");
//            writer.write("MathTest2");
//            writer.write("MathTest3");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        System.out.println("Math 1 Test");
        MathTemp1 t1 = new MathTemp1();
        long[] test1 = perfom1(t1);

//        System.out.println("Math 2 Test");
//        MathTemp2 t2 = new MathTemp2();
//        long[] test2 = perfom2(t2);
//
//        System.out.println("Math 3 Test");
//        MathTemp3 t3 = new MathTemp3();
//        long[] test3 = perfom3(t3);

        makeTestFile(test1);
    }

    private static void makeTestFile(long[] test2) {
        for (int i = 0; i < test2.length; i++) {
            try {
//                writer.write(test1[i] +"");
//                writer.write(test2[i] + "");
                writer.write(test2[i] + "");
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

    private static long[] perfom3(MathTemp3 m3) {
        long[] value = new long[100000];

        for (int j = 0; j < value.length; j++) {
            long s1 = System.nanoTime();

            m3.temp1(11, 22, 33, 44);
            m3.temp2();

            long e1 = System.nanoTime();
            value[j] = e1 - s1;
            System.out.printf("\tavg : %d ", value[j]);
        }

        return value;
    }

}
