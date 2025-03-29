package rain.mocking.design.queue;

import java.util.Random;

public class ArrayQueueTests {
  public static void main(String[] args) {
    ArrayQueue<Integer> queue = new ArrayQueue<>(8);
    Random random = new Random(8L);
    boolean unfull = true;
    while (unfull) {
      unfull = queue.enqueue(random.nextInt(10));
    }
    System.out.println(queue.toString());

    int i = 0;
    while (i < queue.size) {
      queue.dequeue();
      ++i;
      System.out.println(queue.toString());
    }


  }
}
