// EID1: zcc254
// EID2: ka25635

package HW2;

public class FairUnifanBathroom {
	// Bathroom can only hold 5 at a time
	private final int CAPACITY = 5;
	private boolean isUT = false;
	private boolean isOU = false;
	private int numInBathroom = 0;
	private int ticket = 0;
	private int counter = 0;
  	public synchronized void enterBathroomUT() {
  		int myTicket = ticket;
  		ticket++;
		//System.out.println(Thread.currentThread().getName() + " is entering (UT) at: " + System.nanoTime());
		while (numInBathroom >= CAPACITY || isOU || myTicket != counter) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		isUT = true;
		counter++;
		numInBathroom++;
  	}
	
	public synchronized void enterBathroomOU() {
  		int myTicket = ticket;
  		ticket++;
		//System.out.println(Thread.currentThread().getName() + " is entering (OU) at: " + System.nanoTime());
  		while (numInBathroom >= CAPACITY || isUT || myTicket != counter) {
  			try {
  				wait();
			} catch (InterruptedException e) {}
		}
  		isOU = true;
  		counter++;
  		numInBathroom++;
	}
	
	public synchronized void leaveBathroomUT() {
  		if (numInBathroom == 1) {
  			isUT = false;
		}
  		numInBathroom--;
		//System.out.println(Thread.currentThread().getName() + " (UT) leaving at " + System.nanoTime());
  		notifyAll();
	}

	public synchronized void leaveBathroomOU() {
  		if (numInBathroom == 1) {
  			isOU = false;
		}
  		numInBathroom--;
		//System.out.println(Thread.currentThread().getName() + " (OU) leaving at " + System.nanoTime());
  		notifyAll();
	}
}
	
