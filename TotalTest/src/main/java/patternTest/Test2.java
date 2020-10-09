package patternTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Test2 {
    private static BufferedWriter writer;
    public static void main(String[] args) {
        long s1 = 0;
        long e1 = 0;
        long[] testcase = new long[1000000];

        try {
            writer = new BufferedWriter(new FileWriter(new File("Builder.csv")));
            writer.write("builder");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        System.out.print("builder pattern :  ");
        for (int i = 0; i < testcase.length; i++) {
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
