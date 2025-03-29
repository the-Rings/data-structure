package rain.mocking.design.b_tree;

public class BinaryTreeInorderTraversal {
  public void preOrder(TreeNode<?> root) {
    if(root == null) {
      return;
    }
    System.out.print(root.data + "->");
    preOrder(root.left);
    preOrder(root.right);
  }

  public void inOrder(TreeNode<?> root) {
    if(root == null) {
      return;
    }
    inOrder(root.left);
    System.out.print(root.data + "->");
    inOrder(root.right);
  }

  public void postOrder(TreeNode<?> root) {
    if (root == null) {
      return;
    }
    postOrder(root.left);
    postOrder(root.right);
    System.out.print(root.data + "->");
  }

  public static void main(String[] args) {
    BinaryTreeInorderTraversal b = new BinaryTreeInorderTraversal();
    b.preOrder(NodeExample.A);
    System.out.println();
    b.inOrder(NodeExample.B);
    System.out.println();
    b.postOrder(NodeExample.A);
  }

  public static class NodeExample {
    public static TreeNode<String> G = new TreeNode<>("G", null, null);
    public static TreeNode<String> D = new TreeNode<>("D", null, null);
    public static TreeNode<String> E = new TreeNode<>("E", null, null);
    public static TreeNode<String> F = new TreeNode<>("F", null, null);
    public static TreeNode<String> B = new TreeNode<>("B", D, E);
    public static TreeNode<String> C = new TreeNode<>("C", F, G);
    public static TreeNode<String> A = new TreeNode<>("A", B, C);
  }
}
