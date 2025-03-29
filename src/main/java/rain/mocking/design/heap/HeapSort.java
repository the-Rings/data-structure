package rain.mocking.design.heap;

import java.util.Arrays;

public class HeapSort {
  static void swap(int[] arr, int left_index, int right_index) {
    int left_value = arr[left_index];
    arr[left_index] = arr[right_index];
    arr[right_index] = left_value;
  }

  public static void buildHeap(int[] a, int n) {
    for (int i = n/2; i >= 1; --i) {
      heapify(a, n, i);
    }
  }

  private static void heapify(int[] a, int n, int i) {
    while (true) {
      int maxPos  = i;
      if(i*2 <= n && a[i] < a[i*2]) {
        maxPos = i*2;
      }
      if (i*2+1 <= n && a[maxPos] < a[i*2+1]) {
        maxPos = i*2+1;
      }
      if (maxPos == i) break;
      swap(a, i, maxPos);
      i = maxPos;
    }
  }

  public static void sort(int[] heap, int n) {
    int k = n;
    while (k > 1) {
      swap(heap, 1, k);
      --k;
      heapify(heap, k, 1);
    }
  }

  public static void main(String[] args) {
    final int[] arr = {0, 7, 5, 19, 8, 4, 1, 20, 13, 16};
    HeapSort.buildHeap(arr, arr.length - 1);
    System.out.println(Arrays.toString(arr));
//
//    HeapSort.sort(arr, arr.length - 1);
//    System.out.println(Arrays.toString(arr));

  }
}
