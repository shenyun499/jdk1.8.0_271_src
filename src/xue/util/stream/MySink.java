package xue.util.stream;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 连接各个管道
 *
 * @author ：HUANG ZHI XUE
 * @date ：Create in 2022-05-13
 */
public interface MySink extends Consumer<String> {
    default void begin(){}

    default void accept(String str){throw new RuntimeException("invalid call");}
    default void accept(long count){throw new RuntimeException("invalid call");}
    default void accept(Function function){throw new RuntimeException("invalid call");}

    default void end(){}

    abstract class StringSink implements MySink {

        protected MySink nextSink;

        public StringSink(MySink nextSink) {
            this.nextSink = nextSink;
        }
        public StringSink() {
        }

        @Override
        public void begin() {
            nextSink.begin();
        }

        @Override
        public void end() {
            nextSink.end();
        }
    }

    /**
     * count 计数的Sink
     */
    abstract class ReducingSink implements MySink {
        private long state;

        @Override
        public void begin() {
            state = 0;
        }

        @Override
        public void accept(long count) {
            state += count;
        }

        public long getState() {
            return state;
        }
    }
}
