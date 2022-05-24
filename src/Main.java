import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 测试编译是否成功
 *
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2020-12-28
 */
public class Main {
    public static void main2(String[] args) {
        System.out.println("Build Success!");
        HashMap hashMap = new HashMap();
    }

    @Test
    public void main(String[] args) {
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

    @Test
    public void testEveryStep() {
        // 1、为啥用Fuction<?,?>，因为map的作用就是消费原有资源产生新的资源，刚好符合lambda的Fuction作用

        // 2、为啥传入用范型的下界 "? super E"


        // 3、为啥返回要用范型的上界 "? extends R"，因为它可以用顶级父类R接收，但是实际返回子类， 通配符？代表子类
        Stream<String> stringStream = Stream.of("A", "B", "C", "C");
        // 下面例子中可以看到，R代表ParentString顶级父类(当然我们也可以用Object来作为R)，SubStringFilterA代表？，子类
        Stream<ParentString> parentStringStream = stringStream.map(s -> new SubStringFilterA(s));
        Stream<ParentString> parentStringStream1 = stringStream.map(s -> {
            if ("A".equals(s)) {
                return new SubStringFilterA(s);
            }
            return new SubStringFilterB(s);
        });

        // 再补充 上界知识
        // 比如有ParentString、ParentStringA、ParentStringB
        HighLine<ParentString> highLine = new HighLine<>();
        Boolean aBoolean = highLine.testHighLine(Arrays.asList(new SubStringFilterA("1")));
        Boolean bBoolean = highLine.testHighLine(Arrays.asList(new SubStringFilterB("1"), new SubStringFilterB("1")));
    }
}

interface ParentString {public Boolean filter();}
class SubStringFilterA implements ParentString {
    private String value;
    public SubStringFilterA(String value) {this.value = value;};
    @Override
    public Boolean filter() {
        return "A".equals(value);
    }
}
class SubStringFilterB implements ParentString {
    private String value;
    public SubStringFilterB(String value) {this.value = value;};
    @Override
    public Boolean filter() {
        return "B".equals(value);
    }
}
class HighLine<T> {
    // 测试 T 下面的类集合数量为2则true
    public Boolean testHighLine(List<? extends T> list) {
        return list.size() == 2;
    }
}
