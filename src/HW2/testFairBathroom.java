package HW2;

import java.util.Random;

public class testFairBathroom implements Runnable{
    final static int PEOPLE = 13;
    ThreadLocal<Boolean> isUT = new ThreadLocal<>();
    FairUnifanBathroom bathroom = new FairUnifanBathroom();
    public testFairBathroom() {
    }

    public void run() {
        Random random = new Random();
        isUT.set(random.nextBoolean());
        //System.out.println(Thread.currentThread().getName() + ": " + isUT.get());
        if (isUT.get()) {
            bathroom.enterBathroomUT();
            // In Bathroom
            bathroom.leaveBathroomUT();
        } else {
            bathroom.enterBathroomOU();
            // In Bathroom
            bathroom.leaveBathroomOU();
        }

    }

    public static void main(String[] args) {
        Thread[] t = new Thread[PEOPLE];

        for (int i = 0; i < PEOPLE; i++) {
            t[i] = new Thread(new testFairBathroom());
        }

        for (int i = 0; i < PEOPLE; i++) {
            t[i].start();
        }
    }

}
