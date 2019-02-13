package HW2;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
    private ReentrantLock queueLock = new ReentrantLock();
    private Condition notFull = queueLock.newCondition();
    private Condition notEmpty = queueLock.newCondition();

    private LinkedList<Node> queue;
    private final int CAPACITY;
    private int count = 0;
    // 9 is highest priority and 0 is lowest
    public PriorityQueue(int capacity) {
        queue = new LinkedList<>();
        CAPACITY = capacity;
        // Creates a Priority queue with maximum allowed size as capacity
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
            if (count == 0) {
                queue.add(node);
                count++;
                notEmpty.signal();
                return 0;
            }
            while (count == CAPACITY) {
                try {
                    notFull.await();
                } catch (InterruptedException e) {}
            }
        } finally {
            queueLock.unlock();
        }
        Node prev = queue.getFirst(), curr = null;
        try {
            prev.lock.lock();
            curr = prev.next;
            if (curr != null)
                curr.lock.lock();
            if (priority > prev.priority) {
                node.next = prev;
                queue.add(queue.indexOf(prev), node);
                count++;
                queueLock.lock();
                try {
                    notEmpty.signal();
                } finally {
                    queueLock.unlock();
                }
                return 0;
            }
            while (curr != null) {
                if (prev.priority >= priority && curr.priority < priority) {
                    node.next = curr;
                    queue.add(queue.indexOf(curr), node);
                    count++;
                    notEmpty.signal();
                    return queue.indexOf(node);
                }
                prev.lock.unlock();
                prev = curr;
                curr = curr.next;
                if (curr != null)
                    curr.lock.lock();
            }
            queue.add(node);
            count++;
            queueLock.lock();
            try {
                notEmpty.signal();
            } finally {
                queueLock.unlock();
            }
            return queue.indexOf(node);
        } finally {
            if (curr != null)
                curr.lock.unlock();
            prev.lock.unlock();
        }

    }

    public int search(String name) {
        if (count == 0)
            return -1;
        Node prev = queue.getFirst(), curr = null;
        try {
            prev.lock.lock();
            curr = prev.next;
            if (prev.name.equals(name))
                return 0;
            else if (curr == null)
                return -1;
            curr.lock.lock();
            while (curr != null) {
                if (curr.name.equals(name))
                    return queue.indexOf(curr);
                prev.lock.unlock();
                prev = curr;
                curr = curr.next;
                if (curr != null)
                    curr.lock.lock();
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
        queueLock.lock();
        try {
            while (count == 0) {
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {}
            }
            count--;
            notFull.signal();
            return queue.poll().name;
        } finally {
            queueLock.unlock();
        }
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
    }

    public void print() {
        System.out.print("[");
        for (Node n : queue) {
            System.out.print("\"" + n.name + ", " + n.priority + "\" ");
        }
        System.out.println("]");
    }

    public void clear() {
        queue.clear();
    }

    public LinkedList<Node> getQueue() {
        return queue;
    }

    public String peek() {
        return queue.peekFirst().name;
    }

    class Node {
        ReentrantLock lock = new ReentrantLock();
        String name;
        int priority;
        Node next;

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
