package hash;

import java.util.HashMap;
import java.util.Map;

public class LRUCache {
  public static class DLinkedNode {
    public int key;
    public int value;
    public DLinkedNode prev;
    public DLinkedNode next;

    public DLinkedNode(int key, int value) {
      this.key = key;
      this.value = value;
    }
  }

  public Map<Integer, DLinkedNode> cache = new HashMap<>();
  public int size;
  public int capacity;
  public DLinkedNode head;
  public DLinkedNode tail;

  public LRUCache(int capacity) {
    this.size = 0;
    this.capacity = capacity;
    this.head = new DLinkedNode(-1, -1);
    this.tail = new DLinkedNode(-1, -1);
    this.head.prev = null;
    this.head.next = null;
    this.tail.prev = head;
    this.tail.next = null;
  }

  public int get(int key) {
    if (size == 0) {
      return -1;
    }
    DLinkedNode node = cache.get(key);
    if (node == null) {
      return -1;
    }
    removeNode(node);
    addNodeAtHead(node);
    return node.value;
  }

  public void put(int key, int value) {
    DLinkedNode node = cache.get(key);
    if (node != null) {
      node.value = value;
      removeNode(node);
      addNodeAtHead(node);
      return;
    }
    if (size == capacity) {
      cache.remove(tail.prev.key);
      removeNode(tail.prev);
      size--;
    }
    DLinkedNode newNode = new DLinkedNode(key, value);
    addNodeAtHead(newNode);
    cache.put(key, newNode);
    size++;
  }

  public void remove(int key) {
    DLinkedNode node = cache.get(key);
    if (node != null) {
      removeNode(node);
      cache.remove(key);
      --size;
    }
  }

  public void removeNode(DLinkedNode node) {
    node.next.prev = node.prev;
    node.prev.next = node.next;
  }

  public void addNodeAtHead(DLinkedNode node) {
    node.next = head.next;
    head.next.prev = node;
    head.next = node;
    node.prev = head;
  }
}
