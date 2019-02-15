/*
 * EID1: zcc254
 * EID2: ka25635
 */
package HW2;
import jdk.jshell.execution.Util;

public class MonitorCyclicBarrier {

	private int index;
	private int count;
	private int parties;

	public MonitorCyclicBarrier(int parties) {
		this.index = parties - 1;
		this.count = 0;
		this.parties = parties;
	}

	public int await() throws InterruptedException {


		synchronized (this) {
			int threatintdex = index;
			index--;
			count++;

			if( count < this.parties ){ 
					wait();
			}

			if(threatintdex == 0){
				index = this.parties - 1;
				count = 0;
				notifyAll();
			}

			// you need to write this code
			return threatintdex;
		}
	}
}
