package linked_list;

public class SingleLinkedList {
  static class SLinkedNodeExample {
    static SLinkedNode<String> G = new SLinkedNode<>("g", null);
    static SLinkedNode<String> F = new SLinkedNode<>("f", G);
    static SLinkedNode<String> E = new SLinkedNode<>("e", F);
    static SLinkedNode<String> D = new SLinkedNode<>("d", E);
    static SLinkedNode<String> C = new SLinkedNode<>("c", D);
    static SLinkedNode<String> B = new SLinkedNode<>("b", C);
    static SLinkedNode<String> A = new SLinkedNode<>("a", B);
  }

  private SLinkedNode<String> head = SLinkedNodeExample.A;

  public SLinkedNode<String> find(String value) {
    SLinkedNode<String> p = head;
    while (p != null) {
      if (p.data.equals(value)) {
        return p;
      }
      p = p.next;
    }
    return null;
  }

  public void insert(SLinkedNode<String> newNode, SLinkedNode<String> posNode) {
    if (posNode.next != null) {
      newNode.next = posNode.next;
    }
    posNode.next = newNode;
  }

  public void remove(SLinkedNode<String> prevNode, SLinkedNode<String> delNode) {
    if (prevNode == null) {
      head = head.next;
    } else {
      prevNode.next = delNode.next;
      delNode.next = null;
    }
  }
}
