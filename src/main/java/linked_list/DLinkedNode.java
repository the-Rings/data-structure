package linked_list;

public class DLinkedNode<T> {
  public T data;
  public DLinkedNode<T> next;
  public DLinkedNode<T> prev;

  public DLinkedNode(T data, DLinkedNode<T> prev, DLinkedNode<T> next) {
    this.data = data;
    this.next = next;
    this.prev = prev;
  }

}
