package interview.langzhi;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author mao
 * @date 2024/8/26 16:29
 */
public class BinaryTree {
  TreeNode root;

  public BinaryTree() {
    this.root = null;
  }

  public void assignValues(int depth) {
    if (depth <= 0) {
      return;
    }
    this.root = new TreeNode(-1);
    Queue<TreeNode> queue = new LinkedList<>();

    queue.offer(this.root);

    int value = 1;
    int curDepth = 1;

    while (curDepth < depth && queue.peek() != null) {
      int size = queue.size();
      for (int i = 0; i < size; i++) {
        TreeNode curNode = queue.poll();
        curNode.left = new TreeNode(value++);
        curNode.right = new TreeNode(value++);
      }
      curDepth++;
    }
  }
}

class TreeNode {
  int val;
  TreeNode left;
  TreeNode right;

  public TreeNode(int val) {
    this.val = val;
    this.left = null;
    this.right = null;
  }

  public TreeNode(int val, TreeNode left, TreeNode right) {
    this.val = val;
    this.left = left;
    this.right = right;
  }
}
