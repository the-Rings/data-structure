package rain.mocking.design.heap;

public class Heap {
  public int[] arr;
  public int n; // length - 1
  public int count;

  public Heap(int capacity) {
    arr = new int[capacity + 1];
    n = capacity;
    count = 0;
  }

  private static void swap(int[] arr, int leafIndex, int parentIndex) {
    int leafValue = arr[leafIndex];
    arr[leafIndex] = arr[parentIndex];
    arr[parentIndex] = leafValue;
  }

  public void insert(int data) {
    if (count >= n) {
      return;
    }
    ++count;
    arr[count] = data;
    int i = count;

    while (i / 2 > 0 && arr[i] < arr[i / 2]) { // 自下而上
      swap(arr, i, i / 2);
      i = i / 2;
    }
  }

  public int removeTop() {
    if (count == 0) {
      return 0;
    }
    int top = arr[1];
    arr[1] = arr[count];
    --count;
    heapify(arr, count, 1);
    return top;
  }

  private void heapify(int[] a, int n, int i) { // 自上而下
    while (true) {
      int topIndex = i;
      if (i * 2 <= n && a[i * 2] < a[i]) {
        topIndex = i * 2;
      }
      if (i * 2 + 1 <= n && a[i * 2 + 1] < a[topIndex]) {
        topIndex = i * 2 + 1;
      }
      if (topIndex == i) {
        break;
      }
      swap(a, i, topIndex);
      i = topIndex;
    }
  }
}
