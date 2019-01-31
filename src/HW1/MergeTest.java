package HW1;

import java.util.Arrays;

/**
 * Created by Jerry Wang on 2019/1/29.
 */
public class MergeTest {


    public static void main(String args[]){
        int A[] = new int[]{1,3,5,10};
        int B[] = new int[]{2,3,6,8,9, 10};
        int C[] = new int[10];

        PMerge.parallelMerge(A,B,C, 10);

        System.out.println(Arrays.toString(C));
    }




}
