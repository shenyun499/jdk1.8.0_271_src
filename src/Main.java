import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 测试编译是否成功
 *
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2020-12-28
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Build Success!");
        HashMap hashMap = new HashMap();
    }

    public static void main(String[] args) {
        Predicate<String> str = s -> {
            System.out.println(s);
            return !s.equals("A");
        };
        long count = Stream.of("A", "B", "C", "C")
                .filter(str)
                .distinct()
                .sorted() // sorted需要拿到前面所有的元素进行排序后才能进行后面的map，但是filter不需要，它里面只需要记录所有遍历的值，保证不重复就行。
                .map(t -> {
                    System.out.println(t);
                    return t;
                })
                .count();
        Stream<String> stream = Arrays.asList("1").stream();
        Stream<String> stringStream = stream.filter(str);
        // https://xie.infoq.cn/article/a0e6dc7ed0735da5128b6825b
    }
}
