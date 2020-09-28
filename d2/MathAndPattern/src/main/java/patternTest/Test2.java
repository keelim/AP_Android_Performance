package patternTest;

public class Test2 {
    public static void main(String[] args) {
        long s1 = 0;
        long e1 = 0;
        long[] testcase = new long[10];

        System.out.print("builder pattern :  ");
        for (int i = 0; i < testcase.length; i++) {
            s1 = System.nanoTime();
            for (int j = 0; j < 100; j++) {
                LayoutParams2 l1 = new LayoutParams2.Builder(1).build();
                LayoutParams2 l2 = new LayoutParams2.Builder(1).flags(2).build();
                LayoutParams2 l3 = new LayoutParams2.Builder(1).flags(2).format(3).build();
                LayoutParams2 l4 = new LayoutParams2.Builder(1).flags(2).format(3).wh(4, 5).build();
                LayoutParams2 l5 = new LayoutParams2.Builder(1).flags(2).format(3).wh(4, 5).xy(6, 7).build();
            }

            e1 = System.nanoTime();
            testcase[i] = e1 - s1;
            System.out.print(testcase[i] + " ");
        }
    }
}
