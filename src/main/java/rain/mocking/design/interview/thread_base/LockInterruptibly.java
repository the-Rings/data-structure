package interview.thread_base;

import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * ReentranctLock支持在获取锁期间，线程被中断
 *
 * @author mao
 * @date 2024/7/27 19:12
 */
@Slf4j
public class LockInterruptibly {
  public static void main(String[] args) throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    lock.lock();
    Thread t1 =
        new Thread(
            () -> {
              try {
                lock.lockInterruptibly();
              } catch (InterruptedException e) {
                System.out.println("interuptibly");
                e.printStackTrace();
              }
            });
    t1.start();
    t1.interrupt();
  }
}
