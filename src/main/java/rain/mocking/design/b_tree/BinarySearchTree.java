package rain.mocking.design.b_tree;

public class BinarySearchTree {

  public static TreeNode<Integer> find(int data, TreeNode<Integer> root) {
    TreeNode<Integer> p = root;
    while (p != null) {
      if (p.data == data) {
        break;
      }
      p = data > p.data ? p.right : p.left;
    }
    return p;
  }

  public static void insert(int data, TreeNode<Integer> root) {
    if (root == null) {
      root = new TreeNode<>(data);
      return;
    }
    TreeNode<Integer> p = root;
    while (p != null) {
      if (data > p.data) {
        if (p.right == null) {
          p.right = new TreeNode<>(data);
          return;
        }
        p = p.right;
      } else {
        if (p.left == null) {
          p.left = new TreeNode<>(data);
          return;
        }
        p = p.left;
      }
    }
  }

  public static TreeNode<Integer> findMax(TreeNode<Integer> root) {
    TreeNode<Integer> p = root;
    while (true) {
      if (p.right == null) {
        return p;
      }
      p = p.right;
    }
  }

  public static TreeNode<Integer> findMini(TreeNode<Integer> root) {
    TreeNode<Integer> p = root;
    while (true) {
      if (p.left == null) {
        return p;
      }
      p = p.left;
    }
  }

  public static class NodeIntegerExample {
    public static TreeNode<Integer> M = new TreeNode<>(27, null, null);
    public static TreeNode<Integer> L = new TreeNode<>(19, null, null);
    public static TreeNode<Integer> K = new TreeNode<>(66, null, null);
    public static TreeNode<Integer> J = new TreeNode<>(51, null, null);
    public static TreeNode<Integer> I = new TreeNode<>(25, L, M);
    public static TreeNode<Integer> H = new TreeNode<>(16, null, null);
    public static TreeNode<Integer> G = new TreeNode<>(58, J, K);
    public static TreeNode<Integer> F = new TreeNode<>(34, null, null);
    public static TreeNode<Integer> E = new TreeNode<>(18, null, I);
    public static TreeNode<Integer> D = new TreeNode<>(13, null, H);
    public static TreeNode<Integer> C = new TreeNode<>(50, F, G);
    public static TreeNode<Integer> B = new TreeNode<>(17, D, E);
    public static TreeNode<Integer> A = new TreeNode<>(33, B, C);
  }

  public static void main(String[] args) {
    TreeNode<Integer> treeNode = find(19, NodeIntegerExample.A);
    System.out.println(treeNode);

    insert(5, NodeIntegerExample.A);

    System.out.println(findMax(NodeIntegerExample.A));
    System.out.println(findMini(NodeIntegerExample.A));

    BinaryTreeInorderTraversal traversal = new BinaryTreeInorderTraversal();
    traversal.inOrder(NodeIntegerExample.A);
  }
}
