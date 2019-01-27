package HW1;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PMerge implements Callable<Integer> {
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {

        for (int i = 0; i < numThreads; i++) {
            try {
                Future<Integer> f1 = threadPool.submit(new PMerge());


            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

    @Override
    public Integer call() {
        return 0;
    }

}
