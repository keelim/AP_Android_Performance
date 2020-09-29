package patternTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Test1 {
    private static BufferedWriter writer;

    public static void main(String[] args) {
        try {
            writer = new BufferedWriter(new FileWriter(new File("telescoping.csv")));
            writer.write("telescoping");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        long s1 = 0;
        long e1 = 0;
        long[] testcase = new long[1000000];

        System.out.print("telescoping pattern :  ");
        for (int i = 0; i < testcase.length; i++) {
            s1 = System.nanoTime();

 LayoutParams l1 = new LayoutParams();
 LayoutParams l2 = new LayoutParams(2);
 LayoutParams l3 = new LayoutParams(1, 2);
 LayoutParams l4 = new LayoutParams(1, 2, 3);
 LayoutParams l5 = new LayoutParams(1, 2, 3, 4, 5);
 LayoutParams l6 = new LayoutParams(1, 2, 3, 4, 5, 6, 7);

            e1 = System.nanoTime();
            long a = e1 - s1;
            testcase[i] = a;
            try {
                writer.write(a + "");
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print(testcase[i] + " ");
        }
    }
}
