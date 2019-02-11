package HW2;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
    ReentrantLock queueLock = new ReentrantLock();
    Condition notFull = queueLock.newCondition();
    Condition notEmpty = queueLock.newCondition();

    private LinkedList<Node> queue;
    private int CAPACITY;
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
            if (queue.isEmpty()) {
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
            Node prev = queue.get(0), curr = null;
            try {
                prev.lock.lock();
                curr = prev.next;
                curr.lock.lock();
                while (curr.next != null) {
                    if (prev.priority <= priority && curr.priority > priority) {
                        queue.add(queue.indexOf(curr), node);
                        count++;
                        notEmpty.signal();
                        return queue.indexOf(node);
                    }
                    prev.lock.unlock();
                    prev = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                queue.add(node);
                count++;
                notEmpty.signal();
                return queue.indexOf(node);
            } finally {
                curr.lock.unlock();
                prev.lock.unlock();
            }
        } finally {
            queueLock.unlock();
        }
    }

    public int search(String name) {
        if (queue.isEmpty())
            return -1;
        Node prev = queue.get(0), curr = null;
        try {
            prev.lock.lock();
            curr = prev.next;
            curr.lock.lock();
            while (curr.next != null) {
                if (curr.name.equals(name))
                    return queue.indexOf(curr);
                prev.lock.unlock();
                prev = curr;
                curr = curr.next;
                curr.lock.lock();
            }
            if (curr.name.equals(name))         // Name is the last element in the queue
                return queue.indexOf(curr);
            return -1;                          // Name not found
        } finally {
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

    }

}
