package HW1;
import java.util.concurrent.*;
public class PMerge{

    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        Callable t = new MergeThread(A,B,C,executorService);
        try {
            Future<Integer> re = executorService.submit(t);
            re.get();
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
        super();
        this.executor = executor;
        this.C = C;
        this.A = A;
        this.B = B;
        this.i = 0;
        this.j = A.length - 1;
        this.k = 0;
        this.l = B.length - 1;

        this.p = 0;
        this.q = A.length + B.length - 1;

        init(A,B,i,j,k,l);
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
        }else {
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

        //System.out.println(i + "  " + j);

        if( i >= j )
            return null;

        int r = (i + j)/2;
        int s = binarySearch(A, B, r, k, l);
        int t = p + (r - i) + (s - k);
        C[t] = A[r];
        //System.out.println("C"+t + " == " + A[r]);

        Future<Integer> future1 = executor.submit(new MergeThread(A, B, C,    i,  r, k ,s,     p, t, executor));
        Future<Integer> future2 = executor.submit(new MergeThread(A, B, C, r+1, j,s ,l, t+1, q, executor));

        try {
            future1.get();
            future2.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        return -1;
    }

    public static int binarySearch(int[] A, int[] B, int r, int k, int l){

        int start = k;
        int end = l;

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