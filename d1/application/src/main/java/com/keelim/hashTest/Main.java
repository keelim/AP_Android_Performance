import java.util.Random;

public class Main { // 일단 이런식으로 테스트 해서 어떤 결과가 나온지 확인

    public static void main(String[] args) {
        HashTest hashTest = new HashTest();
        Random random = new Random();
        int a = 0;
        while (true){

            int num = random.nextInt(6);
            hashTest.run(num); // Hash Test  call을 하는 방법
            a++;
        }

    }
}
