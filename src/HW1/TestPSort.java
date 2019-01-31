package HW1;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class TestPSort {

    @Test
    public void testSort5() {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        int[] test = {2, 4, 3, 1, 5};
        int[] expected = {1, 2, 3, 4, 5};
        PSort testPSort = new PSort(test, 0, 5);
        long start = System.nanoTime();
        int result = pool.invoke(testPSort);
        System.out.println("Testing with an array of 5 | Time: " + (System.nanoTime() - start));
        for (int i = 0; i < test.length; i++)
            System.out.print(test[i] + " ");
        Assert.assertArrayEquals(expected, test);

    }

    @Test
    public void testSort10() {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(processors);
        ForkJoinPool pool = new ForkJoinPool(processors);
        int[] test = {2, 4, 3, 1, 5, 12, 33, 6, 0, 13};
        int[] expected = {0, 1, 2, 3, 4, 5, 6, 12, 13, 33};
        PSort testPSort = new PSort(test, 0, 10);
        long start = System.nanoTime();
        int result = pool.invoke(testPSort);
        Assert.assertArrayEquals(expected, test);
        System.out.println("\n Testing with array of length 10 | Time: " + (System.nanoTime() - start));
        for (int i = 0; i < test.length; i++)
            System.out.print(test[i] + " ");
        pool.shutdown();
    }

    @Test
    public void testSort100() {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        int[] test = new int[50];
        Random rand = new Random();
        for (int i = 0; i < test.length; i++) {
            test[i] = rand.nextInt(100 + 1);
        }
        PSort testPSort = new PSort(test, 0, test.length);
        long start = System.nanoTime();
        int result = pool.invoke(testPSort);
        System.out.println("\n Testing with array of length 100 | Time: " + (System.nanoTime() - start));
    }

    @Test
    public void testSort10000() {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        int[] test = new int[10000];
        Random rand = new Random();
        for (int i = 0; i < test.length; i++) {
            test[i] = rand.nextInt(10000 + 1);
        }
        PSort testPSort = new PSort(test, 0, test.length);
        long start = System.nanoTime();
        int result = pool.invoke(testPSort);
        System.out.println("\n Testing with array of length 10000 | Time: " + (System.nanoTime() - start));
    }

    @Test
    public void testSort0() {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        int[] test = {};
        PSort testPSort = new PSort(test, 0, test.length);
        long start = System.nanoTime();
        int result = pool.invoke(testPSort);
        System.out.println("\n Testing with array of length 0 | Time: " + (System.nanoTime() - start));
    }

}