package bsearch;

/**
 * @author mao
 * @date 2023/8/15
 */
public class Solution11 {
  // 查找第一个大于等于给定值得元素的索引
  public int bsearch(int[] a, int n, int value) {
    int low = 0;
    int high = n - 1;
    while (low <= high) {
      int mid = low + ((high - low) >> 1);
      if (a[mid] >= value) {
        if ((mid == 0) || (a[mid - 1] < value)) {
          return mid;
        } else {
          high = mid - 1;
        }
      } else {
        low = mid + 1;
      }
    }
    return -1;
  }
}
