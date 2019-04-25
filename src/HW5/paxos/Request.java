package HW5.paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=1L;
    // Your data here
    int seq = -1;
    int prepare = -1;
    Object value = null;


    public Request(int toPropose) {
        this.seq = toPropose;
    }

    // constructor
    public Request(int seq, int prepare, Object value ){
        this.seq = seq;
        this.prepare = prepare;
        this.value = value;
    }

    public Request(int toPropose, Object value) {
        this.seq = toPropose;
        this.value = value;
    }

    public Request(Object value) {
        this.value = value;
    }

    public int getN() {
        return this.seq;
    }

    public Object getV() {
        return this.value;
    }

    // Your constructor and methods here
}
