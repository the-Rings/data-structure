package rain.mocking.design.linked_list;

public class SLinkedNode<T> {
  public T data;
  public SLinkedNode<T> next;

  public SLinkedNode(T data, SLinkedNode<T> next) {
    this.data = data;
    this.next = next;
  }
}
