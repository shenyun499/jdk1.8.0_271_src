import xue.util.stream.MyStream;

import java.util.function.Predicate;

/**
 * 测试类
 *
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2022-05-11
 */
public class MyStreamTest {

    public static void main(String[] args) {
        MyStream of = MyStream.of("1", "2", "3");
        Predicate p = t -> t.equals(1);
        MyStream filter = of.filter(p);
        MyStream map = filter.map(String::toLowerCase);
        long count = map.count();
    }
}
