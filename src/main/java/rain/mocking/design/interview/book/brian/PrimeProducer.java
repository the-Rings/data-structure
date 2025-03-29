package rain.mocking.design.interview.book.brian;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

/**
 * 中断是实现取消的最合理的方式
 *
 * @author mao
 * @date 2024/3/7 12:10
 */
public class PrimeProducer extends Thread {
  private final BlockingQueue<BigInteger> queue;

  PrimeProducer(BlockingQueue<BigInteger> queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    try {
      BigInteger p = BigInteger.ONE;
      while (!Thread.currentThread().isInterrupted()) {
        queue.put(p = p.nextProbablePrime());
      }
    } catch (InterruptedException ignored) {

    }
  }

  public void cancel() {
    interrupt();
  }
}
