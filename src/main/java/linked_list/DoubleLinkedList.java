package linked_list;


public class DoubleLinkedList {
  private static class DLinkedNodeExample {
    static DLinkedNode<String> G = new DLinkedNode<>("g", null, null);
    static DLinkedNode<String> F = new DLinkedNode<>("f", null, G);
    static DLinkedNode<String> E = new DLinkedNode<>("e", null, F);
    static DLinkedNode<String> D = new DLinkedNode<>("d", null, E);
    static DLinkedNode<String> C = new DLinkedNode<>("c", null, D);
    static DLinkedNode<String> B = new DLinkedNode<>("b", null, C);
    static DLinkedNode<String> A = new DLinkedNode<>("a", null, B);

    static {
      G.prev = F;
      G.next = A;
      F.prev = E;
      E.prev = D;
      C.prev = B;
      B.prev = A;
    }
  }

  private DLinkedNode<String> head = DLinkedNodeExample.A;

  public void remove(DLinkedNode<String> delNode) {
    delNode.prev.next = delNode.next;
    delNode.next.prev = delNode.prev;
  }
}
