package HW2;

public class CyclicBarrier {
    // Implement using semaphores

    public CyclicBarrier(int parties) {
        // Creates a new CyclicBarrier that will release threads only when
        // the given number of threads are waiting upon it
    }

    int await() throws InterruptedException {
        // Waits until all parties have invoked await on this CyclicBarrier.
        // If the current thread is not the last to arrive then it is
        // disabled for thread scheduling purposes and lies dormant until
        // the last thread arrives.
        // Returns: the arrival index of the current thread, where index
        // (parties - 1) indicates the first to arrive and zero indicates
        // the last to arrive.
        return 0;
    }


}