package interview;

import java.util.*;

/** 求两个数组的公共元素 */
public class EbaySolution {
  public void a(int[] nums1, int[] nums2) {
    Arrays.sort(nums1);
    Arrays.sort(nums2);
    int m = nums1.length;
    int n = nums2.length;
    int i = 0;
    int j = 0;
    while (i < m && j < n) {
      if (nums1[i] == nums2[j]) {
        int k = j;
        while (nums2[k] == nums1[i]) {
          System.out.println(nums1[i] + "-" + nums2[k]);
          k++;
        }
        i++;
        j++;
      } else if (nums1[i] < nums2[j]) {
        i++;
      } else {
        j++;
      }
    }
  }
}
