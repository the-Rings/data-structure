package interview.langzhi;

/**
 * @author mao
 * @date 2024/8/25 21:11
 */
public class MyLinkedList {
    ListNode dummyHead;
    ListNode tail;

    public MyLinkedList() {
        this.dummyHead = new ListNode(Integer.MIN_VALUE);
        this.tail = null;
    }

    public int get(int index) {
        if (index < 0) {
            return -1;
        }
        int i = 0;
        ListNode p = this.dummyHead;
        while (p.next != null) {
            if (i == index) {
                return p.next.val;
            }
            p = p.next;
            i++;
        }
        return -1;
    }

    public void addAtHead(int val) {
        this.addAtIndex(0, val);
        if (this.tail == null) {
            this.tail = this.dummyHead.next;
        }
    }

    public void addAtTail(int val) {
        // 链表中没有元素
        if (this.dummyHead.next == null) {
            this.addAtHead(val);
            return;
        }
        // 链表中有元素
        this.tail.next = new ListNode(val);
        this.tail = this.tail.next;
    }

    public void addAtIndex(int index, int val) {
        // 添加到头部或者中间
        ListNode p = this.dummyHead;
        ListNode node = new ListNode(val);
        // 链表为空
        if (p.next == null && index < 1) {
            p.next = node;
            this.tail = node;
            return;
        }
        int i = 0;
        while (p.next != null) {
            if (i == index) {
                ListNode tmp = p.next;
                p.next = node;
                node.next = tmp;
                return;
            }
            p = p.next;
            i++;
        }
        // 添加到尾部
        if (i == index) {
            this.addAtTail(val);
        }
    }

    public void deleteAtIndex(int index) {
        // 链表为空
        if (this.dummyHead.next == null) {
            return;
        }
        // 删除头部或者中间或者尾部
        ListNode p = this.dummyHead;
        int i = 0;
        while (p.next != null) {
            if (i == index) {
                p.next = p.next.next;
                break;
            }
            p = p.next;
            i++;
        }
        // 删除了尾部
        if (p.next == null) {
            this.tail = p;
        }

    }


    public static void main(String[] args){
        MyLinkedList myLinkedList = new MyLinkedList();
        myLinkedList.addAtIndex(1, 0);    // 链表变为 1->2->3
        myLinkedList.get(0);              // 返回 2
    }
}

class ListNode {
    public int val;
    public ListNode next;

    public ListNode() {}

    public ListNode(int val) {
        this.val = val;
        this.next = null;
    }
}

/**
 * Your MyLinkedList object will be instantiated and called as such:
 * MyLinkedList obj = new MyLinkedList();
 * int param_1 = obj.get(index);
 * obj.addAtHead(val);
 * obj.addAtTail(val);
 * obj.addAtIndex(index,val);
 * obj.deleteAtIndex(index);
 */
