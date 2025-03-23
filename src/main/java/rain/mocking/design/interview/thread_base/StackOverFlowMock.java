package interview.thread_base;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mao
 * @date 2024/3/10 12:21
 */
public class StackOverFlowMock {
    public static void main(String[] args){
        aVoid();
        new ReentrantLock(true);
    }

    static void aVoid() {
        aVoid();
    }
}
