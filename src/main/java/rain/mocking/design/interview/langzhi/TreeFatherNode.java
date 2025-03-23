package interview.langzhi;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 朗致集团算法面试：
 * 写一个三向链表，然后将三向链表的节点当作二叉树的节点，其中多出来的那个节点指向父节点
 * 根据深度n为二叉树赋值使之形成一个满二叉树
 *
 * @author mao
 * @date 2024/8/26 18:32
 */
@Getter
@Setter
public class TreeFatherNode<T> {
  T val;
  TreeFatherNode<T> father;
  TreeFatherNode<T> leftChild;
  TreeFatherNode<T> rightChild;

  public TreeFatherNode(T val, TreeFatherNode<T> father) {
    this.val = val;
    this.father = father;
    this.leftChild = null;
    this.rightChild = null;
  }

  /**
   * 用队列实现（二叉树层序遍历）
   * @param n 深度为n
   * @return
   */
  public TreeFatherNode<T> assignValues(int n) {
    if (n <= 0) {
      return null;
    }
    Queue<TreeFatherNode<T>> queue = new LinkedList<>();
    int curDepth = 1;
    TreeFatherNode<T> r = new TreeFatherNode<>(null, null);
    queue.offer(r);
    while (curDepth < n && queue.peek() != null) {
      int size = queue.size();
      for (int i = 0; i < size; i++) {
        TreeFatherNode<T> cur = queue.poll();
        cur.leftChild = new TreeFatherNode<>(null, cur);
        queue.offer(cur.leftChild);
        cur.rightChild = new TreeFatherNode<>(null, cur);
        queue.offer(cur.rightChild);
      }
      curDepth++;
    }
    return r;
  }

  /**
   * 用递归实现
   * @param root 根节点
   * @param n 深度n
   */
  public void preorderAssignValues(TreeFatherNode<T> root, int n) {
    if (n <= 0) {
      return;
    }
    root.leftChild = new TreeFatherNode<>(null, root);
    root.rightChild = new TreeFatherNode<>(null, root);
    preorderAssignValues(root.leftChild, --n);
    n++;
    preorderAssignValues(root.rightChild, --n);
  }

  public static void main(String[] args){
    TreeFatherNode<Integer> root = new TreeFatherNode<>(-1, null);
    TreeFatherNode<Integer> r = new TreeFatherNode<>(null, null);
    root.preorderAssignValues(r, 3);
    System.out.println(r);
  }
}
