package xue.util.stream;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 自己实现的Stream类
 *
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2022-05-11
 */
public interface MyStream {

    static MyStream of(String... values) {
        return MyStreamSupport.of(values);
    }

    MyStream filter(Predicate predicate);

    MyStream map(Function<String, String> function);

    long count();
}
