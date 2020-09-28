package patternTest;

public class Test1 {
    public static void main(String[] args) {
        long s1 = 0;
        long e1 = 0;
        long[] testcase = new long[10];

        System.out.print("telescoping pattern :  ");
        for (int i = 0; i < testcase.length; i++) {
            s1 = System.nanoTime();
            for (int j = 0; j < 100; j++) {
                LayoutParams l1 = new LayoutParams();
                LayoutParams l2 = new LayoutParams(2);
                LayoutParams l3 = new LayoutParams(1, 2);
                LayoutParams l4 = new LayoutParams(1, 2, 3);
                LayoutParams l5 = new LayoutParams(1, 2, 3, 4, 5);
                LayoutParams l6 = new LayoutParams(1, 2, 3, 4, 5, 6, 7);
            }
            e1 = System.nanoTime();
            testcase[i] = e1 - s1;
            System.out.print(testcase[i] + " ");
        }
    }
}
