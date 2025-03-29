package rain.mocking.design.queue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

public class ArrayQueue<T> {
  private final T[] items;
  public int size = 0;
  private int head = 0;
  private int tail = 0;

  public ArrayQueue(int capacity) {
    this.items = (T[]) new Object[capacity];
    this.size = capacity;
  }

  public boolean enqueue(T item) {
    if (this.tail == this.size) {
      return false;
    }
    this.items[tail] = item;
    ++tail;
    return true;
  }

  public T dequeue() {
    if (this.head == this.tail) {
      return null;
    }
    T value = this.items[head];
    this.items[head] = null;
    ++head;
    return value;
  }

  public String toString() {
    return Arrays.toString(this.items) + "(head=" + this.head + ",tail=" + this.tail + ")";
  }
}
