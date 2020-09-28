package mathTest;

public class MathTest {
    public static void main(String[] args) {

        System.out.println("Math 1 Test");
        MathTemp1 t1 = new MathTemp1();
        perfom1(t1);

        System.out.println("Math 2 Test");
        MathTemp2 t2 = new MathTemp2();
        perfom2(t2);

        System.out.println("Math 3 Test");
        MathTemp3 t3 = new MathTemp3();
        perfom3(t3);
    }

    private static void perfom1(MathTemp1 m1) {

        long[] testcase = new long[10];

        for (int j = 0; j < 10; j++) {
            long s1 = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                m1.temp1(11, 22, 33, 44);
                m1.temp2();
            }
            long e1 = System.nanoTime();
            testcase[j] = e1 - s1;
            System.out.printf("%d ", testcase[j]);
        }

        long sum = 0;
        for (final long l : testcase) {
            sum += l;
        }

        System.out.printf("avg : %d\n", sum / testcase.length);
    }

    private static void perfom2(MathTemp2 m2) {

        long[] testcase = new long[10];

        for (int j = 0; j < 10; j++) {
            long s1 = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                m2.temp1(11, 22, 33, 44);
                m2.temp2();
            }
            long e1 = System.nanoTime();
            testcase[j] = e1 - s1;
            System.out.printf("%d ", testcase[j]);
        }

        long sum = 0;
        for (final long l : testcase) {
            sum += l;
        }
        System.out.printf("\tavg : %d\n", sum / testcase.length);

    }

    private static void perfom3(MathTemp3 m3) {

        long[] testcase = new long[10];

        for (int j = 0; j < 10; j++) {
            long s1 = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                m3.temp1(11, 22, 33, 44);
                m3.temp2();
            }
            long e1 = System.nanoTime();
            testcase[j] = e1 - s1;
            System.out.printf("\tavg : %d ", testcase[j]);
        }

        long sum = 0;
        for (final long l : testcase) {
            sum += l;
        }
        System.out.printf("  %d\n", sum / testcase.length);

    }

}
