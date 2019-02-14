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
		System.out.println(Thread.currentThread().getName() + " is entering (UT) with ticket: " + myTicket);
		while (numInBathroom >= CAPACITY || isOU || myTicket > counter + numInBathroom) {
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
		System.out.println(Thread.currentThread().getName() + " is entering (OU) with ticket: " + myTicket);
  		while (numInBathroom >= CAPACITY || isUT || myTicket > counter + numInBathroom) {
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
		System.out.println("UT Fan leaving");
  		notifyAll();
	}

	public synchronized void leaveBathroomOU() {
  		if (numInBathroom == 1) {
  			isOU = false;
		}
  		numInBathroom--;
		System.out.println("OU Fan leaving");
  		notifyAll();
	}
}

