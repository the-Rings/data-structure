package rain.mocking.design.queue;

import java.util.Arrays;

public class CircularQueue<T> {
  private final T[] items;
  public int size = 0;
  private int head = 0;
  private int tail = 0;

  public CircularQueue(int capacity) {
    this.items = (T[]) new Object[capacity];
    this.size = capacity;
  }

  public boolean enqueue(T item) {
    if ((this.tail + 1) % this.size == this.head) {
      return false;
    }
    this.items[tail] = item;
    this.tail = (this.tail + 1) % this.size;
    return true;
  }

  public T dequeue() {
    // 判断队列为空
    if (this.head == this.tail) {
      return null;
    }
    T value = items[head];
    this.items[head] = null;
    this.head = (this.head + 1) % this.size;
    return value;
  }

  public String toString() {
    return Arrays.toString(this.items) + "(head=" + this.head + ",tail=" + this.tail + ")";
  }
}
