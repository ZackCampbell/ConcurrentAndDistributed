package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    int n;
    int n_a;
    boolean reject = false;
    boolean isDecided = false;
    Object v;
    Object v_a;
    int done = -1;

    public Response(int done){
        this.reject = true;
        this.done = done;
    }

//    public Response(int seq, Object value, boolean accept, int oldReqNumber, Object oldValue){
//        this.seq = seq;
//        this.value = value;
//        this.accept = accept;
//        this.oldReqNumber = oldReqNumber;
//        this.oldValue = oldValue;
//    }

    public Response(int n, int done) {
        this.done = done;
        this.n = n;
    }

    public Response(Object value, int done) {
        this.done = done;
        this.v = value;
    }

    public Response(Object value, int done, boolean isDecided) {
        this.done = done;
        this.v = value;
        this.isDecided = isDecided;
    }

    public Response(int n, int n_a, Object v_a, int done) {
        this.done = done;
        this.n = n;
        this.n_a = n_a;
        this.done = done;
        this.v_a = v_a;
    }

    public int getN() {
        return this.n;
    }

    public int getNA() {
        return this.n_a;
    }

    public Object getVA() {
        return this.v_a;
    }

    public Object getV() {
        return this.v;
    }

    public boolean getReject() {
        return this.reject;
    }

    public int getDone() {
        return this.done;
    }

    // Your constructor and methods here
}
