package rain.mocking.design.interview.yas;

enum YasTickTypeEnum {
  TRADE,
  QUOTE
}

/**
 * 在Java中，对共享变量的赋值是一个原子操作，但是在多线程并发的情况下，即使赋值操作是原子的，也不能保证线程之间的可见性。
 * 也就是说，一个线程修改了共享变量的值，但其他线程可能无法立即看到这个变化。因此，在并发情况下，访问共享变量时，通常需要使用同步机制来确保线程之间的可见性和一致性。
 *
 * @author mao
 * @date 2024/3/7 10:13
 */
public class WebSocketServerMock {
  // 当一个线程修改了volatile变量的值，其他线程将立即看到这个变化，因此可以避免了并发访问导致的数据不一致性问题。
  static volatile YasTickTypeEnum tickType;

  public static void main(String[] args) {
    Thread kafkaThread = new Thread(new KafkaThread());
    Thread onMessageThread = new Thread(new OnMessageThread());
    tickType = YasTickTypeEnum.TRADE;

    kafkaThread.setName("kafkaThread");
    onMessageThread.setName("onMessageThread");

    onMessageThread.start();
    kafkaThread.start();
  }

  static class KafkaThread implements Runnable {
    @Override
    public void run() {
      int i = 0;
      while (i < 5) {
        System.out.println(Thread.currentThread().getName() + "---" + tickType);
        i++;
      }
      tickType = YasTickTypeEnum.QUOTE;
      i = 0;
      while (i < 5) {
        System.out.println(Thread.currentThread().getName() + "---" + tickType);
        i++;
      }
    }
  }

  static class OnMessageThread implements Runnable {
    @Override
    public void run() {
      int i = 0;
      while (i < 5) {
        System.out.println(Thread.currentThread().getName() + "---" + tickType);
        i++;
      }

      i = 0;
      while (i < 5) {
        System.out.println(Thread.currentThread().getName() + "---" + tickType);
        i++;
      }
    }
  }
}
