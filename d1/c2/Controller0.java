package d1.c2;

import java.util.Random;

public class Controller0 {
    public static final int BAD_TOKEN = -1;

    private final Random random;

    public Controller0() {
        random = new Random();
    }

    public int run() {
        int key = random.nextInt(6); //0, 1, 2, 3, 4
        int ob = random.nextInt(2); //0, 1
        Object objects = (ob == 0) ? new Object() : null;

        if (objects == null) {
            if (key == 0) {
                System.out.println("First come");
                return BAD_TOKEN;
            }
            if (key == 1) {
                System.out.println("First come");
                return BAD_TOKEN;
            }
            if (key == 2) {
                System.out.println("First come");
                return BAD_TOKEN;
            }
            if (key == 3) {
                System.out.println("First come");
                return BAD_TOKEN;
            }
            if (key == 4) {
                System.out.println("First come");
                return BAD_TOKEN;
            }

        } else if (key == 0) {
            System.out.println("Second come");
            return BAD_TOKEN;
        } else if (key == 1) {
            System.out.println("Second come");
            return BAD_TOKEN;
        } else if (key == 2) {
            System.out.println("Second come");
            return BAD_TOKEN;
        } else if (key == 3) {
            System.out.println("Second come");
            return BAD_TOKEN;
        } else if (key == 4) {
            System.out.println("Second come");
            return BAD_TOKEN;
        }
        return 1;
    }
    /// 측정 종료

}
