package xue.util.stream;

/**
 * 专门用来生成Head的工具类
 *
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2022-05-11
 */
public class MyStreamSupport {
    public static MyStream of(String[] str) {
        return new MyPipeline.Head(str);
    }
}
