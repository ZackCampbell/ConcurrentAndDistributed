package HW2;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FairUnifanBathroom {
	// Bathroom can only hold 5 at a time
	private final int CAPACITY = 5;
	private volatile AtomicBoolean isUT = new AtomicBoolean(false);
	private volatile AtomicBoolean isOU = new AtomicBoolean(false);
	private volatile AtomicInteger numInBathroom = new AtomicInteger(0);
	private volatile ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
  	public synchronized void enterBathroomUT() {
  		queue.add(Thread.currentThread().getName());
		System.out.println(Thread.currentThread().getName() + " is entering (UT)");
		while (numInBathroom.get() >= CAPACITY || isOU.get() || !queue.peek().equals(Thread.currentThread().getName())) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		isUT.set(true);
		numInBathroom.getAndIncrement();
  	}
	
	public synchronized void enterBathroomOU() {
		queue.add(Thread.currentThread().getName());
		System.out.println(Thread.currentThread().getName() + " is entering (OU)");
  		while (numInBathroom.get() >= CAPACITY || isUT.get() || !queue.peek().equals(Thread.currentThread().getName())) {
  			try {
  				wait();
			} catch (InterruptedException e) {}
		}
  		isOU.set(true);
  		numInBathroom.getAndIncrement();
	}
	
	public synchronized void leaveBathroomUT() {
  		if (numInBathroom.get() == 1) {
  			isUT.set(false);
		}
  		numInBathroom.getAndDecrement();
  		queue.poll();
		System.out.println("UT Fan leaving");
  		notifyAll();
	}

	public synchronized void leaveBathroomOU() {
  		if (numInBathroom.get() == 1) {
  			isOU.set(false);
		}
  		queue.poll();
  		numInBathroom.getAndDecrement();
		System.out.println("OU Fan leaving");
  		notifyAll();
	}
}

