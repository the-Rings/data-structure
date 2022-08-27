package queue;

import java.util.Random;

public class CircularQueueTests {
  public static void main(String[] args) {
    CircularQueue<Integer> queue = new CircularQueue<>(5);
    Random random = new Random(8L);
    boolean unfull = true;
    while (unfull) {
      unfull = queue.enqueue(random.nextInt(10));
    }

    System.out.println(queue.toString());
    int i = 0;
    while (i < queue.size - 1) {
      queue.dequeue();
      System.out.println(queue.toString());
      ++i;
    }
 
    boolean flag = queue.enqueue(random.nextInt(10));
    System.out.println(flag + "---" + queue.toString());
    flag = queue.enqueue(random.nextInt(10));
    System.out.println(flag + "---" + queue.toString());
  }
}
