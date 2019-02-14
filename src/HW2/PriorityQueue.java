package HW2;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
    private ReentrantLock queueLock = new ReentrantLock();
    private Condition notFull = queueLock.newCondition();
    private Condition notEmpty = queueLock.newCondition();
    private final int CAPACITY;
    private Node head = null;
    private int count = 0;
    // 9 is highest priority and 0 is lowest
    public PriorityQueue(int capacity) {
        CAPACITY = capacity;
    }

    public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
        if (search(name) != -1)
            return -1;
        Node node = new Node(name, priority);
        queueLock.lock();
        try {
            while (count == CAPACITY) {
                try {
                    notFull.await();
                } catch (InterruptedException e) {}
            }
        } finally {
            queueLock.unlock();
        }
        if (count == 0) {                               // Queue is empty
            node.index = 0;
            head = node;
            count++;
            signalCondition(notEmpty);
            return 0;
        }
        Node prev = head, curr = null;
        try {
            prev.lock.lock();
            curr = prev.next;
            if (curr != null)
                curr.lock.lock();
            if (priority > prev.priority) {                 // If new node has highest priority
                node.next = prev;
                node.index = 0;
                prev.index = 1;
                while (curr != null) {          // Update indices
                    curr.lock.lock();
                    curr.index = prev.index + 1;
                    prev.lock.unlock();
                    prev = curr;
                    curr = curr.next;
                }
                head = node;
                count++;
                signalCondition(notEmpty);
                return 0;
            }
            while (curr != null) {
                curr.lock.lock();
                if (prev.priority >= priority && curr.priority < priority) {
                    node.next = curr;
                    prev.next = node;
                    node.index = curr.index;
                    while (curr != null) {          // Update indices
                        curr.lock.lock();
                        curr.index++;
                        prev.lock.unlock();
                        prev = curr;
                        curr = curr.next;
                    }
                    count++;
                    signalCondition(notEmpty);
                    return node.index;
                }
                prev.lock.unlock();
                prev = curr;
                curr = curr.next;
            }
            prev.next = node;
            node.index = prev.index + 1;
            count++;
            signalCondition(notEmpty);
            return node.index;
        } finally {
            if (curr != null)
                curr.lock.unlock();
            prev.lock.unlock();
        }

    }

    public int search(String name) {
        if (count == 0)
            return -1;
        Node prev = head, curr = null;
        try {
            prev.lock.lock();
            curr = prev.next;
            if (prev.name.equals(name))
                return 0;
            else if (curr == null)
                return -1;
            while (curr != null) {
                curr.lock.lock();
                if (curr.name.equals(name))
                    return curr.index;
                prev.lock.unlock();
                prev = curr;
                curr = curr.next;
            }
            return -1;                          // Name not found
        } finally {
            if (curr != null)
                curr.lock.unlock();
            prev.lock.unlock();
        }
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
    }

    public String getFirst() {
        Node prev = head, curr = null;
        queueLock.lock();
        try {
            while (count == 0) {
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {}
            }
        } finally {
            queueLock.unlock();
        }
        try {
            prev.lock.lock();
            curr = prev.next;
            if (curr == null)
                head = null;
            else
                head = curr;
            String result = prev.name;
            while (curr != null) {
                curr.lock.lock();
                curr.index--;
                prev.lock.unlock();
                prev = curr;
                curr = curr.next;
            }
            signalCondition(notFull);
            return result;
        } finally {
            prev.lock.unlock();
            if (curr != null)
                curr.lock.unlock();
        }
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
    }

    private void signalCondition(Condition c) {
        queueLock.lock();
        try {
            c.signal();
        } finally {
            queueLock.unlock();
        }
    }

    public void print() {
        System.out.print("[");
        Node n = head;
        while (n != null) {
            System.out.print("\"" + n.name + ", " + n.priority + "\" ");
            n = n.next;
        }
        System.out.println("]");
    }

    public String peek() {
        return head.name;
    }

    class Node {
        ReentrantLock lock = new ReentrantLock();
        String name;
        int priority;
        Node next;
        int index;

        public Node(String name, int priority) {
            this.name = name;
            this.priority = priority;
            this.next = null;
        }

        public Node(String name, int priority, Node next) {
            this.name = name;
            this.priority = priority;
            this.next = next;
        }

    }

}
