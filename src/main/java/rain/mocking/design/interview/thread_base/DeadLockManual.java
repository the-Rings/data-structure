package interview.thread_base;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mao
 * @date 2024/7/28 16:26
 */
public class DeadLockManual {
  public static void main(String[] args) {
    ReentrantLock lock_main = new ReentrantLock();
    ReentrantLock lock_t0 = new ReentrantLock();

    lock_main.lock();
    Thread t0 =
        new Thread(
            () -> {
              try {
                lock_t0.lock();
                TimeUnit.MILLISECONDS.sleep(1000);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              lock_main.lock();
              System.out.println("t0 get lock_main success.");
            });

    t0.start();
  }
}
