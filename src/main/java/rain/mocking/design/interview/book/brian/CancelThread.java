package interview.book.brian;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 取消一个线程（不是最合理的方式）
 *
 * @author mao
 * @date 2024/3/7 10:32
 */
public class CancelThread {
  public static void main(String[] args) throws InterruptedException {
    PrimeGenerator primeGenerator = new PrimeGenerator();
    Thread primeThread = new Thread(primeGenerator);

    primeThread.start();
    // 素数生成3ms，然后停止
    TimeUnit.MILLISECONDS.sleep(3);
    primeGenerator.cancel();

    System.out.println(primeGenerator.get());
  }
}

class PrimeGenerator implements Runnable {
  private final List<BigInteger> primes = new ArrayList<>();
  private volatile boolean cancelled;

  @Override
  public void run() {
    BigInteger p = BigInteger.ONE;
    while (!cancelled) {
      p = p.nextProbablePrime();
      synchronized (this) {
        primes.add(p);
      }
    }
  }

  public void cancel() {
    cancelled = true;
  }

  public synchronized List<BigInteger> get() {
    return new ArrayList<>(primes);
  }
}
