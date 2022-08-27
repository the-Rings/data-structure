package b_tree;

public class BPlusTreeNode {
  public static int m = 5;
  public int[] keywords = new int[m-1]; //键值，用来划分数据区间
  public BPlusTreeNode[] children = new BPlusTreeNode[m];
}
