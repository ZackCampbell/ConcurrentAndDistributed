package HW1;
import java.util.concurrent.*;
public class PMerge{

    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        Callable t = new MergeThread(A,B,C,executorService);
        try {
            Future<Integer> f = executorService.submit(t);

            f.get();

            executorService.shutdown();

            int temp = 0;

            for(int i = 0, j = C.length-1; i <= j; i++, j--){
                temp = C[i];
                C[i] = C[j];
                C[j] = temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MergeThread implements Callable<Integer> {

    int[] A;
    int[] B;
    int[] C;
    int i;
    int j;
    int l;
    int k;
    int p;
    int q;
    ExecutorService executor;

    public MergeThread(int[] A, int[] B, int[] C, ExecutorService executor){
        //super();
        this.executor = executor;

        this.A = A;
        this.B = B;
        this.C = C;

        // i start index of A, j end index of A
        // k start index of A, l end index of B
        // p start index of A, q end index of C

        this.i = 0;
        this.j = A.length ;

        this.k = 0;
        this.l = B.length ;

        this.p = 0;
        this.q = A.length + B.length - 1;

        init(A,B,i,j,k,l); // if size of A is bigger than B, swap A and B
    }

    public MergeThread(int[] A, int[] B, int[] C, int i, int j,int k, int l,int p,int q, ExecutorService executor){

        this.executor = executor;

        this.C = C;
        this.p = p;
        this.q = q;

        init(A,B,i,j,k,l);
    }

    public void init(int[] A, int[] B, int i, int j,int k, int l) {
        int m = j - i, n = l - k;
        if (m < n){
            this.A = B;
            this.B = A;

            this.i = k;
            this.j = l;

            this.k = i;
            this.l = j;
        }
        else{
            this.A = A;
            this.B = B;
            this.i = i;
            this.j = j;
            this.l = l;
            this.k = k;
        }
    }

    @Override
    public Integer call() {

        if( i >= j )
            return null;

        int r = (i+j)/2;
        int s = binarySearch(A, B, r, k, l);
        int t = p + (r - i) + (s - k);
        C[t] = A[r];

        Future<Integer> f1 = executor.submit(new MergeThread(A, B, C,    i,  r, k ,s,     p, t, executor));
        Future<Integer> f2 = executor.submit(new MergeThread(A, B, C, r+1, j,s ,l, t+1, q, executor));

        try {
            f1.get();
            f2.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int binarySearch(int[] A, int[] B, int r, int k, int l){

        int start = k;
        int end = l-1;

        while(start <= end){
            int mid = (start + end)/2;
            if(A[r] == B[mid]){
                return mid;
            } else if(A[r] < B[mid]){
                end = mid - 1;
            } else{
                start = mid + 1;
            }
        }

        return start;
    }
}
