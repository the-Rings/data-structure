package interview.thread_base;

import java.util.concurrent.Semaphore;

/**
 * 两个线程交替打印
 */
public class PrintingWithSemaphore {
  private static final Semaphore numberSem = new Semaphore(1);
  // 刚开始没有许可permit，只有释放一个许可才能获得许可
  private static final Semaphore letterSem = new Semaphore(0);

  public static void main(String[] args) {
    Thread numT = new Thread(new NumberPrinter());
    Thread letT = new Thread(new LetterPrinter());

    numT.start();
    letT.start();
  }

  static class NumberPrinter implements Runnable {
    @Override
    public void run() {
      for (int i = 1; i < 27; i++) {
        try {
          numberSem.acquire();
          System.out.println(i);
          letterSem.release();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  static class LetterPrinter implements Runnable {
    @Override
    public void run() {
      for (char i = 'A'; i < 'Z'; i++) {
        try {
          letterSem.acquire();
          System.out.println(i);
          numberSem.release();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
