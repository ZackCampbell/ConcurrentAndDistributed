package HW5.paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    int seq;
    int oldReqNumber; // n a (highest accept request # seen)
    Object oldValue;  // v a (highest accept object seen)
    boolean accept = true;
    Object value;

    int[] donelist = null;


    public Response(){
        this.accept = false;
    }

//    public Response(int seq, Object value, boolean accept, int oldReqNumber, Object oldValue){
//        this.seq = seq;
//        this.value = value;
//        this.accept = accept;
//        this.oldReqNumber = oldReqNumber;
//        this.oldValue = oldValue;
//    }

    public Response(int seq, int[] donelist) {
        this.donelist = donelist;
        this.seq = seq;
    }

    public Response(Object value, int[] donelist) {
        this.donelist = donelist;
        this.value = value;
    }

    public Response(int seq, int oldReqNumber, Object value, int[] donelist) {
        this.donelist = donelist;
        this.seq = seq;
        this.oldReqNumber = oldReqNumber;
        this.value = value;
    }

    public int getN() {
        return this.seq;
    }

    public Object getV() {
        return this.value;
    }

    public boolean getAccept() {
        return this.accept;
    }

    public int[] getDonelist() {
        return this.donelist;
    }

    // Your constructor and methods here
}
