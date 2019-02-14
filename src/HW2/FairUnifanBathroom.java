// EID 1
// EID 2

public class FairUnifanBathroom {

	private int UTnum = 0; // number of UT student in bathroom
	private int OUnum = 0; // number of OU student in bathroom
	private int BathroomSize = 5;
	private int EnterNumber = 0; // a reference number of student who enter bathroom
	private int NextLeaveNumber = 0;

	private boolean UTBathroomCheack = false;
	private boolean OUBathroomCheack = false;
	private boolean UTBathroomEmpty = false;
	private boolean OUBathroomEmpty = false;


  public synchronized void enterBathroomUT() throws InterruptedException{

  	    int UTEnterNumber = this.EnterNumber;
	    System.out.println("UT Thread "+ Thread.currentThread().getId() + "   " + UTEnterNumber );
	    this.EnterNumber++;

	    if(UTEnterNumber == NextLeaveNumber)
	  	   UTBathroomCheack = true;

	    // check whether OU students are in bathroom or not
	    while (OUBathroomCheack || UTEnterNumber != this.NextLeaveNumber){
	  	   wait();

	  	   if(UTBathroomEmpty){
	  	   	 UTBathroomEmpty = false;
	  	   	 notifyAll();
	  	   	 break;
		   }
	    }

	    System.out.println(Thread.currentThread().getId() + " pass");
	    NextLeaveNumber++;
	    UTnum++;
	    notifyAll();

  }
	
	public synchronized void enterBathroomOU() throws InterruptedException{

  		int OUEnterNumber = this.EnterNumber;
		System.out.println("OU Thread "+ Thread.currentThread().getId() + "   " + OUEnterNumber );
		this.EnterNumber++;

		if(OUEnterNumber == NextLeaveNumber)
			OUBathroomCheack = true;

		// check whether OU students are in bathroom or not
		while (UTBathroomCheack || OUEnterNumber != this.NextLeaveNumber){
			wait();

			if(OUBathroomEmpty){
				OUBathroomEmpty = false;
				notifyAll();
				break;
			}
		}

		System.out.println(Thread.currentThread().getId() + " pass");
		NextLeaveNumber++;
		OUnum++;
		notifyAll();

	}
	
	public synchronized void leaveBathroomUT() throws InterruptedException{
    // Called when a UT fan wants to leave bathroom
		UTnum--;

		if(UTnum == 0){
			UTBathroomCheack = false;
			UTBathroomEmpty = true;
		}
	}

	public synchronized void leaveBathroomOU() throws InterruptedException{
    // Called when a OU fan wants to leave bathroom
		OUnum--;

		if(OUnum == 0){
			OUBathroomCheack = false;
			OUBathroomEmpty = true;
		}
	}
}
	
