import java.util.HashMap;
import java.util.function.BiConsumer;

public class HashTest {
    private static final int ADD_BAD_TOKEN = 1;
    private HashMap<Integer, BiConsumer<Object, Object>> hashMap;

    public HashTest() {
        hashMap = new HashMap<>();

        BiConsumer biConsumer = (ob1, ob2) ->{
            System.out.println(ob1.toString()+" "+ob2.toString());
        };

        hashMap.put(1, biConsumer);

        biConsumer = (ob1, ob2) ->{
            System.out.println("1"+ob1.toString()+" "+ob2.toString());
        };
        hashMap.put(2, biConsumer);

        biConsumer = (ob1, ob2) ->{
            System.out.println("2"+ob1.toString()+" "+ob2.toString());
        };
        hashMap.put(3, biConsumer);

        biConsumer = (ob1, ob2) ->{
            System.out.println("3"+ob1.toString()+" "+ob2.toString());
        };
        hashMap.put(4, biConsumer);

        biConsumer = (ob1, ob2) ->{
            System.out.println("4"+ob1.toString()+" "+ob2.toString());
        };
        hashMap.put(5, biConsumer);

        biConsumer = (ob1, ob2) ->{
            System.out.println("5"+ob1.toString()+" "+ob2.toString());
        };
        hashMap.put(6, biConsumer);

    }

    public int run(int a){
        BiConsumer <Object, Object> bi = hashMap.get(a);
        if(bi!=null){
            bi.accept(new Object(), new Object());
            return ADD_BAD_TOKEN;
        }

        return 0;
    }


}
