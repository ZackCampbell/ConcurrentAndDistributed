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
    int n = -1;
    int seq = 0;
    int prepare = -1;
    Object value = null;


    public Request(int toPropose, int seq) {
        this.n = toPropose;
        this.seq = seq;
    }

    // constructor
    public Request(int n, int prepare, Object value, int seq){
        this.n = n;
        this.prepare = prepare;
        this.value = value;
        this.seq = seq;
    }

    public Request(int toPropose, Object value, int seq) {
        this.n = toPropose;
        this.value = value;
        this.seq = seq;
    }

    public Request(Object value, int seq) {
        this.value = value;
        this.seq = seq;
    }

    public int getN() {
        return this.n;
    }

    public int getSeq() {
        return this.seq;
    }

    public Object getV() {
        return this.value;
    }

    // Your constructor and methods here
}
