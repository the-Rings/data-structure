package b_tree;

public class BPlusTreeNode {
  public static int m = 5;
  public int[] keywords = new int[m - 1];
  public BPlusTreeNode[] children = new BPlusTreeNode[m];
}
