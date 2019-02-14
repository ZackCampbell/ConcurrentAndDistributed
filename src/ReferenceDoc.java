import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReferenceDoc {
    public static void main(String[] args) {
        System.out.println("Hello World");
        //  Test various algorithms here

        // For HelloWorldThread
        // HelloWorldThread t = new HelloWorldThread();
        // t.start();

        // For RunnableExampleBar
        // RunnableExampleBar r1 = new RunnableExampleBar("Romeo");
        // Thread t1 = new Thread(r1);
        // t1.start();
        // RunnableExampleBar r2 = new RunnableExampleBar("Romeo");
        // Thread t2 = new Thread(r2);
        // t2.start();

        // For Fibonacci using join
//        Fibonacci f1 = new Fibonacci(8);
//        f1.start();
//        try {
//            f1.join();
//        } catch (InterruptedException e) {}
//        System.out.println("Answer is " + f1.getResult());

        // For Fibonacci using Callable
//        try {
//            ExecutorService es = Executors.newSingleThreadExecutor();
//            Fibonacci2 f = new Fibonacci2(8);
//            Future<Integer> f1 = es.submit(f);
//            System.out.println("Answer is: " + f1.get());
//            es.shutdown();
//            f.threadPool.shutdown();
//        } catch (Exception e) {
//            System.err.println(e);
//        }

        // For Fibonacci using Fork
//        int processors = Runtime.getRuntime().availableProcessors();
//        System.out.println("Number of processors: " + processors);
//        Fibonacci3 f = new Fibonacci3(8);
//        ForkJoinPool pool = new ForkJoinPool(processors);
//        int result = pool.invoke(f);
//        System.out.println("Result: " + result);

        // For TestMutualExclusion
//        TestMutualExclusion t[];
//        int N = 8;
//        t = new TestMutualExclusion[N];
//        Lock lock = new Bakery(N); // Or any other mutex algorithm
//        for (int i = 0; i < N; i++) {
//            t[i] = new TestMutualExclusion(i, lock);
//            t[i].start();
//        }

        // Producer-Consumer Algorithm using Semaphores
//        BoundedBuffer buffer = new BoundedBuffer();
//        Producer producer = new Producer(buffer);
//        Consumer consumer = new Consumer(buffer);

        // Dining philosopher using semaphores
//        DiningPhilosopher dp = new DiningPhilosopher(5);
//        for (int i = 0; i < 5; i++)
//            new Philosopher(i, dp);

        // Dining philosopher using monitors
//        DiningMonitor dm = new DiningMonitor(5);
//        for (int i = 0; i < 5; i++)
//            new Philosopher(i, dm);
    }
}

/**
 * Fibonacci using "join"
 */
class Fibonacci extends Thread{
    int n;
    int result;
    public Fibonacci(int n) {
        this.n = n;
    }
    public void run() {
        if (n == 0 || n == 1) result = 1;
        else {
            Fibonacci f1 = new Fibonacci(n-1);
            Fibonacci f2 = new Fibonacci(n-2);
            f1.start();
            f2.start();
            try {
                f1.join();
                f2.join();
            } catch (InterruptedException e) {}
            result = f1.getResult() + f2.getResult();
        }
    }
    public int getResult() {
        return result;
    }
}

/**
 * Fibonacci using Callable
 */
class Fibonacci2 implements Callable<Integer> {
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    int n;
    public Fibonacci2(int n) {
        this.n = n;
    }
    public Integer call() {
        try {
            if((n == 0)||(n == 1 )) return 1;
            else {
                Future<Integer> f1 = threadPool.submit(new Fibonacci2(n-1));
                Future<Integer> f2 = threadPool.submit(new Fibonacci2(n-2));
                return f1.get() + f2.get();
            }
        } catch (Exception e) {
            System.err.println(e);
            return 1;
        }
    }
}

/**
 * Fibonacci using Fork
 */
class Fibonacci3 extends RecursiveTask<Integer> {
    final int n;
    public Fibonacci3(int n) {
        this.n = n;
    }
    protected Integer compute() {
        if (n == 0 || n == 1) return 1;
        Fibonacci3 f1 = new Fibonacci3(n-1);
        f1.fork();
        Fibonacci3 f2 = new Fibonacci3(n-2);
        return f2.compute() + f1.join();
    }
}

class HelloWorldThread extends Thread {
    public void run() {
        System.out.println("Hello World");
    }
}

/**
 * Example using Runnable
 */
class RunnableExample {
    String name;
    public RunnableExample(String s) {
        name = s;
    }
    public void setName(String s) {
        name = s;
    }
    public String getName() {
        return name;
    }
}
class RunnableExampleBar extends RunnableExample implements Runnable {
    public RunnableExampleBar(String s) {
        super(s);
    }
    public void run() {
        for (int i = 0; i < 10; i++)
            System.out.println(getName() + " Hello World");
    }
}

class TestMutualExclusion extends Thread {
    int myId;
    Lock lock;
    Random r = new Random();
    public TestMutualExclusion(int id, Lock lock) {
        myId = id;
        this.lock = lock;
    }
    void nonCriticalSection() {
        System.out.println(myId + " is not in CS");
        Util.mySleep(r.nextInt(1000));
    }
    void CriticalSection() {
        System.out.println(myId + " is in CS***");
        Util.mySleep(r.nextInt(1000));
    }
    public void run() {
        while (true) {
            lock.requestCS(myId);
            CriticalSection();
            lock.releaseCS(myId);
            nonCriticalSection();
        }
    }
}

interface Lock {
    void requestCS(int pid);
    void releaseCS(int pid);
}
interface myLock {
    void lock();
    void unlock();
}

/**
 * An attempt that violates mutual exclusion
 */
class Attempt1 implements Lock {
    boolean openDoor = true;
    public void requestCS(int i) {
        while (!openDoor)
            openDoor = false;
    }
    public void releaseCS(int i) {
        openDoor = true;
    }
}

/**
 * An attempt that can deadlock
 */
class Attempt2 implements Lock {
    boolean wantCS[] = {false, false};
    public void requestCS(int i) {      // Entry Protocol
        wantCS[i] = true;   // Declare Intent
        while (wantCS[1-i]);    // Busy Wait
    }
    public void releaseCS(int i) {
        wantCS[i] = false;
    }

}

/**
 * An attempt with strict alternation
 */
class Attempt3 implements Lock {
    int turn = 0;
    public void requestCS(int i) {
        while (turn == 1-i);
    }
    public void releaseCS(int i) {
        turn = 1-i;
    }
}

class PetersonAlgorithm implements Lock {
    boolean wantCS[] = {false, false};
    int turn = 1;
    public void requestCS(int i) {
        int j = 1 - i;
        wantCS[i] = true;
        turn = j;
        while (wantCS[j] && (turn == j));
    }
    public void releaseCS(int i) {
        wantCS[i] = false;
    }
}

/**
 * Utilizes multiple Peterson's algorithms to get the answer
 */
class FilterAlgorithm implements Lock {
    int N;
    int[] gate;
    int[] last;
    public FilterAlgorithm(int numProc) {
        N = numProc;
        gate = new int[N];  // We only use gate[1]... gate[N-1]; gate[0] is unused
        Arrays.fill(gate, 0);
        last = new int[N];
        Arrays.fill(last, 0);
    }
    public void requestCS(int i) {
        for (int k = 1; k < N; k++) {
            gate[i] = k;
            last[k] = i;
            for (int j = 0; j < N; j++) {
                while ((j != i) &&          // There is some other process
                        (gate[j] >= k) &&   // that is ahead or at the same level
                        (last[k] == i)      // and I am the last to update last[k]
                ) {}    // Busy Wait
            }
        }
    }
    public void releaseCS(int i) {
        gate[i] = 0;
    }
}

/**
 * Lamport's Bakery Algorithm
 */
class Bakery implements Lock {
    int N;
    boolean[] choosing;     // Inside Doorway
    int[] number;
    public Bakery(int numProc) {
        N = numProc;
        choosing = new boolean[N];
        number = new int[N];
        for (int j = 0; j < N; j++) {
            choosing[j] = false;
            number[j] = 0;
        }
    }
    public void requestCS(int i) {
        // Step 1: Doorway: Choose a number
        choosing[i] = true;
        for (int j = 0; j < N; j++) {
            if (number[j] > number[i])
                number[i] = number[j];
        }
        number[i]++;
        choosing[i] = false;

        // Step 2: Check if my number is the smallest
        for (int j = 0; j < N; j++) {
            while (choosing[j]);    // Process j in doorway
            while ((number[j] != 0) &&
                    ((number[j] < number[i]) ||
                            ((number[j] == number[i]) && j < i))
            );      // Busy Wait
        }
    }
    public void releaseCS(int i) {
        number[i] = 0;
    }
}

/**
 * Fischer's mutual exclusion algorithm
 */
class Fischer implements Lock {
    int N;
    volatile int turn;
    final int delta;
    public Fischer(int numProc) {
        N = numProc;
        turn = -1;
        delta = 5;
    }
    public void requestCS(int i) {
        while (true) {
            while (turn != -1) {}
            turn = i;
            try {
                Thread.sleep(delta);
            } catch (InterruptedException e) {}
            if (turn == i) return;
        }
    }
    public void releaseCS(int i) {
        turn = -1;
    }
}

/**
 * Splitter algorithm
 */
class Splitter {
    private boolean door;
    private int last;
    public Splitter() {
        door = true;
        last = -1;
    }
    public String split(int i) {
        if (!door) {
            return "Left";
        } else {
            door = false;
            if (last == i)
                return "Down";
            else
                return "Right";
        }
    }
}

/**
 * Lamport's Fast Mutex Algorithm
 */
class FastMutex {
    private volatile int X, Y;
    private volatile boolean[] flag;
    public FastMutex(int nSRMWRegisters) {
        X = -1;
        Y = -1;
        flag = new boolean[nSRMWRegisters];
    }

    @SuppressWarnings("all")
    public void acquire(int i) {
        while (true) {
            flag[i] = true;
            X = i;
            if (Y != -1) {      // Splitter left
                flag[i] = false;
                while (Y != -1) {}
            } else {
                Y = i;
                if (X == i) {   // Success with splitter
                    return;     // Fast Path
                } else {        // Splitter Right
                    flag[i] = false;
                    for (int j = 1; j < flag.length; j++) {
                        while (flag[j]){}
                    }
                    if (Y == i)
                        return; // Slow Path
                    else {
                        while (Y != -1){}
                    }
                }
            }
        }
    }

    public void release(int i) {
        Y = -1;
        flag[i] = false;
    }
}

/**
 * Building Locks
 */
class GetAndSet implements myLock {
    AtomicBoolean isOccupied = new AtomicBoolean(false);
    public void lock() {
        while (isOccupied.getAndSet(true)) {
            Thread.yield();
            // Skip
        }
    }
    public void unlock() {
        isOccupied.set(false);
    }
}

/**
 * If it doesn't succeed in getAndSet, it spins on the 'get' operation
 * Faster than just GetAndSet
 */
class GetAndGetAndSet implements myLock {
    AtomicBoolean isOccupied = new AtomicBoolean(false);
    public void lock() {
        while (true) {
            while (isOccupied.get()) {}
            if (!isOccupied.getAndSet(true)) return;
        }
    }
    public void unlock() {
        isOccupied.set(false);
    }
}

class MutexWithBackoff implements myLock {
    AtomicBoolean isOccupied = new AtomicBoolean(false);
    Random r = new Random();
    public void lock() {
        while (true) {
            while (isOccupied.get()) {}
            if (!isOccupied.getAndSet(true)) return;
            else {
                int timeToSleep = r.nextInt(1000);
                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {}
            }
        }
    }
    public void unlock() {
        isOccupied.set(false);
    }
}

/**
 * NOT COMPLETE
 */
class TicketMutex implements myLock {
    AtomicInteger nextTicket = new AtomicInteger(0);
    AtomicInteger currentTicket = new AtomicInteger(0);
    public void lock(){
        int myticket = nextTicket.getAndIncrement();
        while(myticket != currentTicket.get()){}    // skip
    }
    public void unlock(){
        int temp = currentTicket.getAndIncrement();
    }
}

/**
 * Uses O(mn) space
 */
class AndersonLock implements myLock {
    AtomicInteger tailSlot = new AtomicInteger(0);
    int n;
    volatile boolean[] available;
    ThreadLocal<Integer> mySlot; //     Initialize to 0
    public AndersonLock(int n) {
        this.n = n;
        available = new boolean[n];
        Arrays.fill(available, false);
        available[0] = true;
        try {
            mySlot.set(0);
        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
    public void lock() {
        mySlot.set(tailSlot.getAndIncrement() % n);
        while (!available[mySlot.get()]) {}
    }
    public void unlock() {
        available[mySlot.get()] = false;
        available[(mySlot.get() + 1) % n] = true;
    }
}

/**
 * CLHLock for mutex
 */
class CLHLock implements myLock {
    class Node {
        boolean locked;
    }
    AtomicReference<Node> tailNode;
    ThreadLocal<Node> myNode;
    ThreadLocal<Node> pred;

    public CLHLock() {
        tailNode = new AtomicReference<>(new Node());
        tailNode.get().locked = false;
        myNode = new ThreadLocal<>() {
            protected Node initialValue() {
                return new Node();
            }
        };
        pred = new ThreadLocal<>();
    }
    public void lock() {
        myNode.get().locked = true;
        pred.set(tailNode.getAndSet(myNode.get()));
        while (pred.get().locked) {
            Thread.yield();
        }
    }
    public void unlock() {
        myNode.get().locked = false;
        myNode.set(pred.get());     // Reusing predecessor node for future use
    }
}

class MCSLock implements myLock {
    class QNode {
        boolean locked;
        QNode next;
        QNode() {
            locked = true;
            next = null;
        }
    }
    AtomicReference<QNode> tailNode = new AtomicReference<>(null);
    ThreadLocal<QNode> myNode;

    public MCSLock() {
        myNode = new ThreadLocal<>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }
    public void lock() {
        QNode pred = tailNode.getAndSet(myNode.get());
        if (pred != null) {
            myNode.get().locked = true;
            pred.next = myNode.get();
            while (myNode.get().locked) {
                Thread.yield();
            }
        }
    }
    public void unlock() {
        if (myNode.get().next == null) {
            if (tailNode.compareAndSet(myNode.get(), null)) return;
            while (myNode.get().next == null) {
                Thread.yield();
            }
        }
        myNode.get().next.locked = false;
        myNode.get().next = null;
    }
}

/**
 * Reads and returns old value in memory and replaces it with a new value
 */
class TestAndSet {
    int myValue = -1;
    public synchronized int testAndSet(int newValue) {
        int oldValue = myValue;
        myValue = newValue;
        return oldValue;
    }
}

/**
 * Mutual exclusion using testAndSet
 */
class HWMutex implements Lock {
    TestAndSet lockFlag;
    public void requestCS(int i) {      // Entry Protocol
        while (lockFlag.testAndSet(1) == 1);
    }
    public void releaseCS(int i) {      // Exit Protocol
        lockFlag.testAndSet(0);
    }
}

/**
 * Swaps two memory locations in one atomic step
 */
class Synch {
    public static synchronized void swap(Boolean m1, Boolean m2) {
        Boolean temp = m1;
        m1 = m2;
        m2 = temp;
    }
}

class LFilter implements Lock {
    int n;
    int l;
    int[] gate;
    int[] last;
    public LFilter(int n, int l) {
        this.n = n;
        this.l = l;
        if (0 >= n - l + 1) {
            gate = new int[0];
            last = new int[0];
        } else {
            gate = new int[n - l + 1];
            last = new int[n - l + 1];
        }
        Arrays.fill(gate, 0);
        Arrays.fill(last, 0);
    }

    public void requestCS(int i) {
        for (int k = 1; k < n - l + 1; k++) {
            gate[i] = k;
            last[k] = i;
            int higher = l + 1;
            while (higher > l && last[k] == i) {
                higher = 0;
                for (int j = 0; j < n; j++) {
                    if (gate[k] >= i)
                        higher++;
                }
            }
        }
    }

    public void releaseCS(int i) {
        gate[i] = 0;
    }
}

// ------------------------------------ Chapter 3 ------------------------------------

class BinarySemaphore {
    boolean value;
    public BinarySemaphore(boolean initValue) {
        value = initValue;
    }
    public synchronized void P() {
        while (!value) {
            Util.myWait(this);  // in the queue of blocked processes
        }
        value = false;
    }
    public synchronized void V() {
        value = true;
        notify();
    }
}

class CountingSemaphore {
    int value;
    public CountingSemaphore(int initValue) {
        value = initValue;
    }
    public synchronized void P() {
        while (value == 0) Util.myWait(this);
        value--;
    }
    public synchronized void V() {
        value++;
        notify();
    }
}


/**
 * Bounded buffer using semaphores
 */
class BoundedBuffer {
    final int size = 10;
    Object[] buffer = new Object[size];
    int inBuf = 0, outBuf = 0;
    BinarySemaphore mutex = new BinarySemaphore(true);
    CountingSemaphore isEmpty = new CountingSemaphore(0);
    CountingSemaphore isFull = new CountingSemaphore(size);

    public void deposit(Object value) {
        isFull.P();     // Wait if buffer is full
        mutex.P();      // Ensures mutual exclusion
        buffer[inBuf] = value;      // Update the buffer
        inBuf = (inBuf + 1) % size;
        mutex.V();
        isEmpty.V();    // Notify any waiting customer
    }

    public Object fetch() {
        Object value;
        isEmpty.P();    // Wait if buffer is empty
        mutex.P();      // Ensures mutual exclusion
        value = buffer[outBuf];     // Read from buffer
        outBuf = (outBuf + 1) % size;
        mutex.V();
        isFull.V();     // Notify any waiting producer
        return value;
    }
}

class Producer implements Runnable {
    BoundedBuffer b = null;
    public Producer(BoundedBuffer initb) {
        b = initb;
        new Thread(this).start();
    }
    @Override
    public void run() {
        Double item;
        Random r = new Random();
        while (true) {
            item = r.nextDouble();
            System.out.println("produced_item_" + item);
            b.deposit(item);
            Util.mySleep(200);
        }
    }
}

class Consumer implements Runnable {
    BoundedBuffer b = null;
    public Consumer(BoundedBuffer initb) {
        b = initb;
        new Thread(this).start();
    }

    @Override
    public void run() {
        Double item;
        while (true) {
            item = (Double)b.fetch();
            System.out.println("fetched_item_" + item);
            Util.mySleep(50);
        }
    }
}

/**
 * ReaderWriter algorithm using semaphores
 */
class ReaderWriter {
    int numReaders = 0;
    BinarySemaphore mutex = new BinarySemaphore(true);
    BinarySemaphore wlock = new BinarySemaphore(true);
    public void startRead() {
        mutex.P();
        numReaders++;
        if (numReaders == 1)
            wlock.P();
        mutex.V();
    }
    public void endRead() {
        mutex.P();
        numReaders--;
        if (numReaders == 0)
            wlock.V();
        mutex.V();
    }
    public void startWrite() {
        wlock.P();
    }
    public void endWrite() {
        wlock.V();
    }

}

interface Resource {
    public void acquire(int i);
    public void release(int i);
}

class Philosopher implements Runnable {
    int id = 0;
    Resource r = null;
    public Philosopher(int initId, Resource initr) {
        id = initId;
        r = initr;
        new Thread(this).start();
    }
    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Phil_" + id + "_thinking");
                Thread.sleep(30);
                System.out.println("Phil_" + id + "_hungry");
                r.acquire(id);
                System.out.println("Phil_" + id + "_eating");
                Thread.sleep(40);
                r.release(id);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

class DiningPhilosopher implements Resource {
    int n = 0;
    BinarySemaphore[] fork = null;
    public DiningPhilosopher(int initN) {
        n = initN;
        fork = new BinarySemaphore[n];
        for (int i = 0; i < n; i++) {
            fork[i] = new BinarySemaphore(true);
        }
    }

    @Override
    public void acquire(int i) {
        fork[i].P();
        fork[(i + 1) % n].V();
    }

    @Override
    public void release(int i) {
        fork[i].V();
        fork[(i + 1) % n].V();
    }
}

class BoundedBufferMonitor {
    final int size = 10;
    Object[] buffer = new Object[size];
    int inBuf = 0, outBuf = 0, count = 0;
    public synchronized void deposit(Object value) {
        while (count == size)   // Buffer full
            Util.myWait(this);
        buffer[inBuf] = value;
        inBuf = (inBuf + 1) % size;
        count++;
        if (count == 1)     // Items available for fetch
            notify();
    }
    public synchronized Object fetch() {
        Object value;
        while (count == 0)  // Buffer empty
            Util.myWait(this);
        value = buffer[outBuf];
        outBuf = (outBuf + 1) % size;
        count--;
        if (count == size - 1)      // Empty slots available
            notify();
        return value;
    }
}

/**
 * Bounded buffer monitor for multiple producers and consumers
 */
class MultiBoundedBufferMonitor {
    final int size = 10;
    double[] buffer = new double[size];
    int inBuf = 0, outBuf = 0, count = 0;
    public synchronized void deposit(double value) {
        while (count == size)   // Buffer full
            Util.myWait(this);
        buffer[inBuf] = value;
        inBuf = (inBuf + 1) % size;
        count++;
        notifyAll();
    }
    public synchronized double fetch() {
        double value;
        while (count == 0)  // Buffer empty
            Util.myWait(this);
        value = buffer[outBuf];
        outBuf = (outBuf + 1) % size;
        count--;
        notifyAll();
        return value;
    }
}

/**
 * Dining philosopher using monitors
 */
class DiningMonitor implements Resource{
    int n = 0;
    int state[] = null;
    static final int thinking = 0, hungry = 1, eating = 2;
    public DiningMonitor(int initN) {
        n = initN;
        state = new int[n];
        for (int i = 0; i < n; i++)
            state[i] = thinking;
    }
    int left(int i) {
        return (n + i - 1) % n;
    }
    int right(int i) {
        return (i + 1) % n;
    }
    @Override
    public synchronized void acquire(int i) {
        state[i] = hungry;
        test(i);
        while (state[i] != eating)
            Util.myWait(this);
    }
    @Override
    public synchronized void release(int i) {
        state[i] = thinking;
        test(left(i));
        test(right(i));
    }
    void test(int i) {
        if ((state[left(i)] != eating) && (state[i] == hungry) && (state[right(i)] != eating)) {
            state[i] = eating;
            notifyAll();
        }
    }
}

/**
 * Bounded buffer using reentrant locks and conditions
 */
class MBoundedBufferMonitor {
    final int size = 10;
    final ReentrantLock monitorLock = new ReentrantLock();
    final Condition notFull = monitorLock.newCondition();
    final Condition notEmpty = monitorLock.newCondition();
    final Object[] buffer = new Object[size];
    int inBuf = 0, outBuf = 0, count = 0;
    public void put(Object x) throws InterruptedException {
        monitorLock.lock();
        try {
            while (count == buffer.length)
                notFull.await();
            buffer[inBuf] = x;
            inBuf = (inBuf + 1) % size;
            count++;
            notEmpty.signal();
        } finally {
            monitorLock.unlock();
        }
    }

    public Object take() throws InterruptedException {
        monitorLock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            Object x = buffer[outBuf];
            outBuf = (outBuf + 1) % size;
            count--;
            notFull.signal();
            return x;
        } finally {
            monitorLock.unlock();
        }
    }
}

/**
 * Linked list
 */
class ListQueue {
    class Node {
        public String data;
        public Node next;
    }
    Node head = null, tail = null;
    public synchronized void enqueue(String data) {
        Node temp = new Node();
        temp.data = data;
        temp.next = null;
        if (tail == null) {
            tail = temp;
            head = tail;
        } else {
            tail.next = temp;
            tail = temp;
        }
        notify();
    }
    public synchronized String dequeue() {
        while (head == null)
            Util.myWait(this);
        String returnval = head.data;
        if (head == tail)
            tail = null;
        head = head.next;
        return returnval;
    }
}

/**
 * CAN RESULT IN DEADLOCKS
 */
class BCell {
    int value;
    public synchronized int getValue() {
        return value;
    }
    public synchronized void setValue(int i) {
        value = i;
    }
    public synchronized void swap(BCell x) {
        int temp = getValue();
        setValue(x.getValue());
        x.setValue(temp);
    }
}

/**
 * AVOIDS DEADLOCK
 */
class Cell {
    int value;
    public synchronized int getValue() {
        return value;
    }
    public synchronized void setValue(int i) {
        value = i;
    }
    protected void doSwap(Cell x) {
        int temp = getValue();
        setValue(x.getValue());
        x.setValue(temp);
    }
    public void swap(Cell x) {
        if (this == x)
            return;
        else if (System.identityHashCode(this) < System.identityHashCode(x)) {
            synchronized (this) {
                synchronized (x) {
                    this.doSwap(x);
                }
            }
        }
        else
            synchronized (x) {
                synchronized (this) {
                    x.doSwap(this);
                }
            }

    }
}

// ----------------------------- Chapter 5: Wait-Free Syncronization ------------------------------

class SafeBoolean {
    boolean value;
    public boolean getValue() {
        return value;
    }
    public void setValue(boolean b) {
        value = b;
    }
}

class RegularBoolean {
    boolean prev;   // not shared
    SafeBoolean value;
    public boolean getValue() {
        return value.getValue();
    }
    public void setValue(boolean b) {
        if (prev != b) {
            value.setValue(b);
            prev = b;
        }
    }
}

class MultiValued {
    int n = 0;
    boolean A[] = null;
    public MultiValued(int maxVal, int initVal) {
        n = maxVal;
        A = new boolean[n];
        for (int i = 0; i < n; i++)
            A[i] = false;
        A[initVal] = true;
    }
    public int getValue() {
        int j = 0;
        while (!A[j]) j++;  // Forward scan
        int v = j;
        for (int i = j - 1; i >=0; i--) {     // Backward scan
            if (A[i])
                v = i;
        }
        return v;
    }
    public void setValue(int x) {
        A[x] = true;
        for (int i = x - 1; i >= 0; i--)
            A[i] = false;
    }
}

class SRSW {
    int value;
    int ts;
    public synchronized int getValue() {
        return value;
    }
    public synchronized int getTS() {
        return ts;
    }
    public synchronized void setValue(int x, int seq) {
        value = x;
        ts = seq;
    }
    public synchronized void setValue(SRSW x) {
        value = x.getValue();
        ts = x.getTS();
    }
}

class MRSW {
    public int val = 0, ts = 0, pid = 0;
    int n = 0;
    SRSW V[] = null;    // value written for reader i
    SRSW Comm[][] = null;
    int seqNo = 0;
    public MRSW(int readers, int initVal) {
        n = readers;
        V = new SRSW[n];
        for (int i = 0; i < n; i++) {
            V[i].setValue(initVal, 0);
        }
        Comm = new SRSW[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Comm[i][j].setValue(initVal, 0);
            }
        }
    }
    public int getValue(int r) {        // Reader r reads
        // read your own register
        SRSW tsv = V[r];    // tsv is local
        // Find the value with the largest timestamp
        for (int i = 0; i < n; i++) {
            if (Comm[i][r].getTS() > tsv.getTS()) {
                tsv = Comm[i][r];
            }
        }

        // Inform other readers
        for (int i = 0; i < n; i++) {
            Comm[r][i].setValue(tsv);
        }
        return tsv.getValue();
    }
    public void setValue(int x) {       // Accessed by the writer
        // Write the value with the larger timestamp
        seqNo++;
        for (int i = 0; i < n; i++) {
            V[i].setValue(x, seqNo);
        }
    }
    public synchronized void setValue(int x, int seq, int id) {
        val = x;
        ts = seq;
        pid = id;
    }
}

class MultiWriter {
    int n = 0;
    MRSW V[] = null;    // Value written by the writer i
    public MultiWriter(int writers, int initVal) {
        n = writers;
        V = new MRSW[n];
        for (int i = 0; i < n; i++) {
            V[i].setValue(initVal, 0, i);
        }
    }
    public int getValue() {
        MRSW tsv = V[0];    // tsv is local
        for (int i = 1; i < n; i++) {
            if ((tsv.ts < V[i].ts) || ((tsv.ts == V[i].ts) && (tsv.pid < V[i].pid)))
                tsv = V[i];
        }
        return tsv.val;
    }
    public void setValue(int w, int x) {
        int maxseq = V[0].ts;
        for (int i = 0; i < n; i++) {
            if (maxseq < V[i].ts)
                maxseq = V[i].ts;
        }
        V[w].setValue(x, maxseq + 1, w);
    }
}

// ----------------------------- Chapter 6: Concurrent Data Structures ------------------------------

class Node<T> {
    T value;
    Node<T> next = null;
    public Node(T val) {
        this.value = val;
    }
}

class LockFreeStack<T> {
    AtomicReference<Node<T>> top = new AtomicReference<Node<T>>(null);
    public void push(T value) {
        Node<T> node = new Node<T>(value);
        while (true) {
            Node<T> oldTop = top.get();
            node.next = oldTop;
            if (top.compareAndSet(oldTop, node)) return;
            else Thread.yield();
        }
    }
    public T pop() throws NoSuchElementException {
        while(true) {
            Node<T> oldTop = top.get();
            if(oldTop == null) throw new NoSuchElementException();
            T val = oldTop.value;
            Node<T> newTop = oldTop.next;
            if(top.compareAndSet(oldTop, newTop)) return val;
            else Thread.yield();
        }
    }
}

class MyAtomicInteger extends AtomicInteger{
    public MyAtomicInteger(int val) {super(val);}
    public int myAddAndGet(int delta) {
        for (;;) {
            int current = get();
            int next = current + delta;
            if (compareAndSet(current, next))
                return next;
        }
    }
}

class SingleQueue {
    int head = 0;
    int tail = 0;
    Object[] items;
    public SingleQueue(int size) {
        head = 0;
        tail = 0;
        items = new Object[size];
    }
    public void put(Object x) {
        while (tail - head == items.length) {} // Busy Wait
        items[tail % items.length] = x;
        tail++;
    }
    public Object get() {
        while (tail - head == 0) {} // Busy Wait
        Object x = items[head % items.length];
        head++;
        return x;
    }
}

// Lock Based Stack
class Stack<T> {
    Node<T> top = null;
    public synchronized void push(T value) {
        Node<T> node = new Node<>(value);
        node.next = top;
        top = node;
    }
    public synchronized T pop() throws NoSuchElementException {
        if (top == null) {
            throw new NoSuchElementException();
        } else {
            Node<T> oldTop = top;
            top = top.next;
            return oldTop.value;
        }
    }
}

class UnboundedQueue<T> {
    ReentrantLock enqLock, deqLock;
    Node<T> head;
    Node<T> tail;
    int size;
    public UnboundedQueue() {
        head = new Node<>(null);
        tail = head;
        enqLock = new ReentrantLock();
        deqLock = new ReentrantLock();
    }
    public T deq() throws EmptyStackException {
        T result;
        deqLock.lock();
        try {
            if (head.next == null) {
                throw new EmptyStackException();
            }
            result = head.next.value;
            head = head.next;
        } finally {
            deqLock.unlock();
        }
        return result;
    }
    public void enq(T x) {
        if (x == null) throw new NullPointerException();
        enqLock.lock();
        try {
            Node<T> e = new Node<>(x);
            tail.next = e;
            tail = e;
        } finally {
            enqLock.unlock();
        }
    }
}