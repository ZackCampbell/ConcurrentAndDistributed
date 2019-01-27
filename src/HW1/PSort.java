package HW1;

import java.util.concurrent.RecursiveTask;

public class PSort extends RecursiveTask<Integer> {

    private int[] A;
    private int begin;
    private int end;

    public PSort(int[] A, int begin, int end) {
        this.A = A;
        this.begin = begin;
        if (end >= A.length)
            this.end = A.length - 1;
        else
            this.end = end;
    }

    public static void parallelSort(int[] A, int begin, int end) {
        if (begin >= end)
            return;
        int pivot = partition(A, begin, end);
        PSort s1 = new PSort(A, begin, pivot);
        s1.fork();
        PSort s2 = new PSort(A, pivot + 1, end);
        s2.compute();
        s1.join();
    }

    public static int partition(int[] A, int begin, int end) {
//        int pivot = A[end];
//        int i = begin - 1;
//        for (int j = begin; j < end; j++) {
//            if (A[j] <= pivot) {
//                swap(A, ++i, j);
//            }
//        }
//        swap(A, ++i, end);
//        return i;

        int i = begin - 1;
        int j = end + 1;
        int pivot = A[begin];
        while (true) {
            do {
                i++;
            } while (A[i] < pivot);
            do {
                j--;
            } while (A[j] > pivot);
            if (i >= j)
                return j;
            swap(A, i, j);
            if (A[i] == A[j] && A[i] == pivot)
                return ++j;
        }
    }

    public static void swap(int[] A, int i, int j) {
        int temp = A[i];
        A[i] = A[j];
        A[j] = temp;
    }

    private static void printArray(int[] A) {
        System.out.print("[");
        for(int i : A) {
            System.out.print(" " + i + ", ");
        }
        System.out.println("]");
    }

    public Integer compute() {
        parallelSort(A, begin, end);
        return 0;
    }

}
