package rain.mocking.design.heap;

import java.util.Arrays;

/**
 * 使用堆，合并数组并将合并后的数组有序输出
 * 1. 建堆
 * 2. 移除堆顶元素
 *
 */
public class MergeArraysUserHeap {
  public static void test(int array[], String str) {
    System.out.println(Arrays.toString(array));
  }
  public static void main(String[] args) {

    int[] a = {6, 16, 26, 46, 106, 111, 112, 136};
    int[] b = {5, 36, 46, 95, 98, 99, 100};
    int[] c = {2, 3, 5, 7, 9, 11, 13, 17, 19};

    test(a, "");

    int capacity = a.length + b.length + c.length;
    Heap heap = new Heap(a.length + b.length + c.length);
    Arrays.stream(a).forEach(heap::insert);
    Arrays.stream(b).forEach(heap::insert);
    Arrays.stream(c).forEach(heap::insert);

    System.out.println(Arrays.toString(heap.arr));

    int i = 1;
    while (i <= capacity) {
      System.out.println(heap.removeTop());
      i++;
    }
  }
}
