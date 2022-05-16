package xue.util.stream;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/**
 * 管道类，主要负责连通各个管道，一个双向链表
 * 一个管道代表着一个操作，如map/filter等
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2022-05-11
 * 管道有以下特性：
 * 1、管道进行分类，有一个有状态的管道类、无状态的管道类和一个头节点管道类
 * 2、一个管道必须保存上下管道，形成双向链表
 * 3、可以保存当前管道的层数
 * 4、需要保存原始数据，将数据保存在头节点比较好
 *
 */
public abstract class MyPipeline implements MyStream {
    // 上一个管道
    private MyPipeline previousPipeline;
    // 下一个管道
    private MyPipeline nextPipeline;
    // 当前管道在第几层，方便后面进行Sink连接
    private Integer level;
    // 保存原数据，暂时只能存String数组，后面单独创建一个对象来放
    private String[] str;

    public MyPipeline(){}

    public MyPipeline(MyPipeline previousPipeline) {
        this.previousPipeline = previousPipeline;
        previousPipeline.nextPipeline = this;
        this.nextPipeline = null;
        this.level = previousPipeline.getLevel() + 1;
    }

    public MyPipeline( Integer level, String[] str) {
        this.previousPipeline = null;
        this.nextPipeline = null;
        this.level = level;
        this.str = str;
    }

    public Integer getLevel() {
        return level;
    }
    abstract MySink onWrap(MySink mySink);

    abstract static class StateLess extends MyPipeline {
        public StateLess(MyPipeline myPipeline) {
            super(myPipeline);
        }
    }
    static abstract class StateFull extends MyPipeline {}

    static class Head extends MyPipeline {
        public Head(String[] str) {
            super(0, str);
        }

        @Override
        MySink onWrap(MySink mySink) {
            throw new RuntimeException("invalid call");
        }
    }
    @Override
    public MyStream filter(Predicate predicate) {
        return new StateLess(this) {
            @Override
            MySink onWrap(MySink mySink) {
                return new MySink.StringSink(mySink) {
                    @Override
                    public void accept(String str) {
                        if (predicate.test(str)) {
                            nextSink.accept(str);
                        }
                    }
                };
            }
        };
    }

    @Override
    public MyStream map(Function<String, String> function) {
        return new StateLess(this) {
            @Override
            MySink onWrap(MySink mySink) {
                return new MySink.StringSink() {
                    @Override
                    public void accept(String str) {
                        nextSink.accept(function.apply(str));
                    }
                };
            }
        };
    }

    public MyPipeline mapToLong(ToLongFunction<String> mapper) {
        return new StateLess(this) {
            @Override
            MySink onWrap(MySink mySink) {
                return new MySink.StringSink() {
                    @Override
                    public void accept(String str) {
                        nextSink.accept(mapper.applyAsLong(str));
                    }
                };
            }
        };
    }

    public MyPipeline countSink() {
        return new StateLess(this) {
            @Override
            MySink onWrap(MySink mySink) {
                return new MySink.ReducingSink() {};
            }
        };
    }

    @Override
    public long count() {
        return mapToLong(e -> 1L).sum();
    }

    public long sum() {
        return evaluate(countSink());
    }

    public long evaluate(MyPipeline myPipeline) {
        return 0;
    }

    long wrapAndCopyInto(MySink mySink) {
        return copyInto(wrapSink(Objects.requireNonNull(mySink)));
    }

    private long copyInto(MySink wrapSink) {
        return 0;
    }

    private MySink wrapSink(MySink requireNonNull) {
        return null;
    }
}
