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

    public String[] getStr() {
        return str;
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
                return new MySink.StringSink(mySink) {
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
                return new MySink.StringSink(mySink) {
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
        // 将元素转成1，sum是最后一个Sink，用来计数
        return mapToLong(e -> 1L).sum();
    }

    public long sum() {
        return evaluate(countSink());
    }

    public long evaluate(MyPipeline myPipeline) {
        // 拿到最后一个Sink，后面可以开始做包装所有的Sink
        MySink lastSink = myPipeline.onWrap(null);
        // 还是调用最后一个Sink，得到最大值
        return ((MySink.ReducingSink)wrapAndCopyInto(lastSink)).getState();
    }

    MySink wrapAndCopyInto(MySink mySink) {
        copyInto(wrapSink(Objects.requireNonNull(mySink)));
        return mySink;
    }

    private void copyInto(MySink wrapSink) {
        // 管道开始，会调用所有无状态的begin
        wrapSink.begin();
        // 遍历一遍元素，调用accept开始消费
        String[] str = this.getSource();
        for (String s : str) {
            wrapSink.accept(s);
        }
        // 结束操作
        wrapSink.end();
    }

    /**
     * 包装所有的sink，形成单链表具体每个 Sink的实现会有nextSink
     * @param mySink 最后一个sink，终结
     * @return
     */
    private MySink wrapSink(MySink mySink) {

        Objects.requireNonNull(mySink);
        for (MyPipeline p = this; p.getLevel() > 0; p = p.previousPipeline) {
            // 高度为0说明p是head的head，不需要包装，可以跳出
            // 高度不为0，实现Sink的管道 也就是调用管道的onWrap方法
            mySink = p.onWrap(mySink);
        }
        // 返回head 的下一个Sink
        return mySink;
    }

    private String[] getSource() {
        MyPipeline p = this;
        for (; p.getLevel() > 0; p = p.previousPipeline) {
        }
        return p.getStr();
    }
}
