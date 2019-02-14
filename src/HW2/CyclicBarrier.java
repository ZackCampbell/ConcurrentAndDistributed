/*
 * EID's of group members
 *
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class CyclicBarrier {

	private int parties;
	private int index;
	private Semaphore sem1, sem2, sem3;


	public CyclicBarrier(int parties) {
		this.parties = parties;
		this.index= parties-1;
		this.sem1 = new Semaphore(1);
		this.sem2 = new Semaphore(0);
		this.sem3 = new Semaphore(0);

	}

	public int await() throws InterruptedException {
		int threadindex = 0;

		// to assign index to each thread
		sem1.acquire();
		threadindex = index;
		index--;
		sem1.release();

		// decide whether to release the thread
		if(threadindex == 0){

			for(int i = 0 ; i < parties - 1; i++)
				sem2.release();

		}else{
			sem2.acquire();
		}


		// change waiting threads number
		sem1.acquire();
		index++;
		sem1.release();

		if(index == parties - 1 ){

			for(int i = 0 ; i < parties - 1; i++)
				sem3.release();

		}else{

			sem3.acquire();
		}

		return threadindex;
	}
}
