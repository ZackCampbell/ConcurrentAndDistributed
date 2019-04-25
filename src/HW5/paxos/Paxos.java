package HW5.paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    Map<Integer, Storage> storageMap;
    Storage storage;
    int seq = -1;
    int n = -1;
    int totalPeers;
    int[] doneList;

    // Your data here


    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.storageMap = new ConcurrentHashMap<Integer, Storage>();
        //this.valueMap = new ConcurrentHashMap<Integer, Object>();
        this.storage = new Storage();
        this.totalPeers = peers.length;
        this.doneList = new int[peers.length];
        for(int i = 0; i < peers.length; i++){
            doneList[i] = -1;
        }


        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
        if (seq >= Min() && this.storage.getCurrentState() != State.Decided) {
            this.seq = seq;
            storage.setAcceptedValue(value);
            storageMap.put(seq, storage);
            Thread newInstance = new Thread(this);
            newInstance.start();
        }
    }

    @Override
    public void run(){

        while (this.Status(this.seq).state != State.Decided) {
            int toPropose = this.Min();
            ArrayList<Response> prepResponseList = new ArrayList<>();
            ArrayList<Response> accResponseList = new ArrayList<>();
            for (int id : this.ports) {                               // Send "Prepare" request to all peers
                Response prepResponse = this.Call("Prepare", new Request(toPropose), id);
                prepResponseList.add(prepResponse);
            }
            int prepCounter = 0;
            int currHighestProposal = -1;
            Object highestValue = null;
            for (Response r : prepResponseList) {
                if (r != null) {
                    prepCounter++;
                    if (r.getN() > currHighestProposal) {
                        currHighestProposal = r.getN();
                        highestValue = r.getV();
                    }
                }
            }
            if (prepCounter > prepResponseList.size()) {
                prepResponseList.clear();
                if (storage.getHighestAccept() <= currHighestProposal) {
                    storage.setAcceptedValue(highestValue);
                }
                int accCounter = 0;
                for (int id : this.ports) {                           // Send "Accept" request to all peers
                    Response accResponse = this.Call("Accept", new Request(toPropose, storage.getAcceptedValue()), id);
                    accResponseList.add(accResponse);
                }
                for (Response r : accResponseList) {
                    if (r != null) {
                        accCounter++;
                    }
                }
                if (accCounter > accResponseList.size()) {
                    for (int id : this.ports) {
                        Response decResponse = this.Call("Decide", new Request(storage.getAcceptedValue()), id);
                    }
                }
            }
        }
        }


    // RMI handler
    public Response Prepare(Request req){

        if (req.getN() >= storage.getHighestPromise()) {
            storage.setHighestPromise(req.getN());
            return new Response(req.getN(), storage.getHighestAccept(), this.storage.getAcceptedValue());
        } else {
            return new Response();
        }
    }

    public Response Accept(Request req){
        if (req.getN() >= storage.getHighestPromise()) {
            storage.setHighestPromise(req.getN());
            storage.setHighestAccept(req.getN());
            storage.setAcceptedValue(req.getV());
            return new Response(req.getN());
        } else {
            return new Response();
        }
    }

    public Response Decide(Request req){
        this.storage.setCurrentState(State.Decided);
        this.storage.setAcceptedValue(req.getV());
        return new Response(req.getV());
        // your code here

    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        this.doneList[this.me] = seq;
        // Your code here
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
        int max = -1;
        for(int n : storageMap.keySet()) {
            if (n > max) {
                max = n;
            }
        }
        return max;

    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min() {
        // Your code here
        int min = Integer.MAX_VALUE;
        for(int n : doneList){
            if(n < min)
                min = n;
        }
        return (min+1);
    }

    public void forgot(){
        int min = Min();
        for(int n : storageMap.keySet()){
            if(n < min)
                storageMap.remove(n);
        }
    }

    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        return new retStatus(this.storage.currentState, this.storage.acceptedValue);
        // Your code here

    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }


    class Storage {
        int highestPromise = -1;        // n_p
        int highestAccept = -1;         // n_a
        //int highestProposal = -1;
        State currentState;
        Object acceptedValue = null;    // v and v_a

        public Storage() {
            this.currentState = State.Pending;
        }

        public State getCurrentState() {
            return this.currentState;
        }

        public void setCurrentState(State currentState) {
            this.currentState = currentState;
        }

        public int getHighestAccept() {
            return this.highestAccept;
        }

        public int getHighestPromise() {
            return this.highestPromise;
        }

        public Object getAcceptedValue() {
            return this.acceptedValue;
        }

        public void setAcceptedValue(Object v) {
            this.acceptedValue = v;
        }

        public void setHighestPromise(int n) {
            this.highestPromise = n;
        }

        public void setHighestAccept(int n) {
            this.highestAccept = n;
        }

    }

}
