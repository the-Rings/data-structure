package interview.thread_base;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 模拟使用单个变量保存取消状态，而导致无法取消线程的场景
 *
 * @author mao
 * @date 2024/3/7 10:45
 */
public class BrokenPrimeProducer extends Thread {
  private final BlockingQueue<BigInteger> queue;
  private volatile boolean cancelled = false;

  BrokenPrimeProducer(BlockingQueue<BigInteger> queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    try {
      BigInteger p = BigInteger.ONE;
      while (!cancelled) {
        // 由于超过队列长度而阻塞在put方法上，导致线程无法结束
        queue.put(p = p.nextProbablePrime());
      }
    } catch (InterruptedException consumed) {

    }
  }

  public void cancel() {
    cancelled = true;
  }

  static void consumerPrimes() throws InterruptedException {
    BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<>(20);
    BrokenPrimeProducer producer = new BrokenPrimeProducer(primes);
    producer.start();
    try {
      // 模拟让生产者产生很多元素，达到阻塞队列上限，导致队列阻塞在put方法上
      TimeUnit.MILLISECONDS.sleep(5000);
      int i = 0;
      while (i < 5) {
        // 消费不完队列中的元素（模拟消费者不能够跟上生产者的速度）
        System.out.println(primes.take());
        i++;
      }
    } finally {
      producer.cancel();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    BrokenPrimeProducer.consumerPrimes();
  }
}
