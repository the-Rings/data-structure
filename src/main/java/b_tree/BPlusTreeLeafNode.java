package b_tree;

public class BPlusTreeLeafNode {
  public static int k = 3;
  public int[] keywords = new int[k];
  public long[] dataAddress = new long[k];
  public BPlusTreeNode prev;
  public BPlusTreeNode next;

}
