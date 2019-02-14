package HW2;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class testPriorityQueue {
    private PriorityQueue q;
    private Random rand = new Random();

    private boolean isSorted(LinkedList<PriorityQueue.Node> q) {
        for (int i = 1; i < q.size(); i++) {
            if (q.get(i).priority > q.get(i-1).priority)
                return false;
        }
        return true;
    }


    @Before
    public void setUp() {
        q = new PriorityQueue(5);
    }

    @Test
    public void sanityTest() {
        q.add("TestSeqEntry", 2);
        q.add("TestSeqEntry2", 1);
        q.add("TestSeqEntry3", 4);
        q.add("TestSeqEntry4", 9);
        q.add("TestSeqAddAtEnd", 0);
//        assert(isSorted(q.getQueue()));
        String expectedFirst = q.peek();
        String first = q.getFirst();
        assertEquals(expectedFirst, first);
//        assert(isSorted(q.getQueue()));
//        System.out.println("Testing adding over capacity\n-----------------------");
//        q.add("TestingEntry5", 5);
//        q.add("TestingEntry6", 6);
//        q.print();
        int search1 = q.search("TestSeqEntry");
        q.print();
        assertEquals(1, search1);
//        q.clear();
    }

    @Test
    public void testPQueueAdd() {

    }

    @Test
    public void testPQueueRemove() {

    }

}
