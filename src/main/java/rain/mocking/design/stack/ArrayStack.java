package rain.mocking.design.stack;

import java.util.Arrays;

public class ArrayStack<T> {
  private int count;
  private final int size;
  private final T[] items;

  public ArrayStack(int capacity) {
    this.size = capacity;
    this.items = (T[])new Object[capacity];
    this.count = 0;
  }

  public Boolean push(T item) {
    if (count == size) {
      return false;
    }
    items[count] = item;
    ++count;
    return true;
  }

  public T pop() {
    if (count == 0) {
      return null;
    }
    T value = items[count - 1];
    items[count - 1] = null;
    --count;
    return value;
  }

  public String toString() {
    return Arrays.toString(items);
  }
}
