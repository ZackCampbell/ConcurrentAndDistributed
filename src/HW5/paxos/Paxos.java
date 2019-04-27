package HW5.paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    AtomicInteger minimum;

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
        this.minimum = new AtomicInteger();
        this.minimum.set(-1);
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
        while (this.Status(this.me).state != State.Decided) {
            int toPropose = this.storage.getHighestPromise() + 1;
            ArrayList<Response> prepResponseList = new ArrayList<>();
            ArrayList<Response> accResponseList = new ArrayList<>();
            for (int id = 0; id < this.ports.length; id++) {                               // Send "Prepare" request to all peers
                Response prepResponse;
                if (!isunreliable() && id != this.me)
                    prepResponse = this.Call("Prepare", new Request(toPropose, storage.getAcceptedValue()), id);
                else
                    prepResponse = Prepare(new Request(toPropose, storage.getAcceptedValue()));
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
//            storage.setHighestPromise(currHighestProposal);
            if (prepCounter > prepResponseList.size() / 2) {
                prepResponseList.clear();
                if (storage.getHighestAccept() <= currHighestProposal) {
                    storage.setAcceptedValue(highestValue);
                    storageMap.put(this.seq, storage);
                }
                int accCounter = 0;
                for (int id = 0; id < this.ports.length; id++) {                           // Send "Accept" request to all peers
                    Response accResponse;
                    if (!isunreliable() && id != this.me)
                        accResponse = this.Call("Accept", new Request(toPropose, storage.getAcceptedValue()), id);
                    else
                        accResponse = Accept(new Request(toPropose, storage.getAcceptedValue()));
                    accResponseList.add(accResponse);
                }
                for (Response r : accResponseList) {
                    if (r != null) {
                        accCounter++;
                    }
                }
                if (accCounter > accResponseList.size() / 2) {
                    accResponseList.clear();
                    for (int id = 0; id < this.ports.length; id++) {
                        Response decResponse;
                        if (!isunreliable() && id != this.me)
                            decResponse = this.Call("Decide", new Request(toPropose, storage.getAcceptedValue()), id);
                        else
                            decResponse = Decide(new Request(toPropose, storage.getAcceptedValue()));
                    }
                }
            }
        }
    }


    // RMI handler
    public Response Prepare(Request req){
        if (req.getN() >= storage.getHighestPromise()) {
            storage.setHighestPromise(req.getN());
            return new Response(req.getN(), storage.getHighestAccept(), this.storage.getAcceptedValue(), this.minimum.get());
        } else {
            return new Response(this.minimum.get());
        }
    }

    public Response Accept(Request req){
        if (req.getN() >= storage.getHighestPromise()) {
            storage.setHighestPromise(req.getN());
            storage.setHighestAccept(req.getN());
            storage.setAcceptedValue(req.getV());
            storageMap.put(this.seq, storage);
            return new Response(req.getN(), this.minimum.get());
        } else {
            return new Response(this.minimum.get());
        }
    }

    public Response Decide(Request req){
        storage.setCurrentState(State.Decided);
        storage.setAcceptedValue(req.getV());
        storageMap.put(this.seq, storage);
//        updateDoneList(req.getDone(), req.getN());
        return new Response(req.getV(), this.minimum.get());
        // your code here

    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        int min = Math.max(minimum.get(), 0);
        for (int i = min; i <= seq; i++) {
//            while (storageMap.get(i).getCurrentState() != State.Decided) {
//                System.out.println("State at " + i + " has not been decided");
//            }
            minimum.set(i);
        }

        //forget();
        // Your code here
    }

    public void updateDoneList(int[] otherList, int otherSeq) {
        if (doneList[otherSeq] < otherList[otherSeq])
            doneList[otherSeq] = otherList[otherSeq];
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
        return max; //

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
//        int min = doneList[this.me];
//        for(int n : doneList){
//            if(n < min)
//                min = n;
//        }
////        this.n = min + 1;
//        return (min+1);
        return forget() + 1;
    }

    public int forget(){
        int min = minimum.get();
        for (int n : storageMap.keySet()) {
            if(n < min)
                min = n;

        }
        for (int key : storageMap.keySet()) {
            if (key < min)
                storageMap.remove(min);
        }
        return min;
    }

    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        if (seq < minimum.get()) {
            return new retStatus(State.Forgotten, this.storage.acceptedValue);
        }
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

        public synchronized State getCurrentState() {
            return this.currentState;
        }

        public synchronized void setCurrentState(State currentState) {
            this.currentState = currentState;
        }

        public synchronized int getHighestAccept() {
            return this.highestAccept;
        }

        public synchronized int getHighestPromise() {
            return this.highestPromise;
        }

        public synchronized Object getAcceptedValue() {
            return this.acceptedValue;
        }

        public synchronized void setAcceptedValue(Object v) {
            this.acceptedValue = v;
        }

        public synchronized void setHighestPromise(int n) {
            this.highestPromise = n;
        }

        public synchronized void setHighestAccept(int n) {
            this.highestAccept = n;
        }

    }

}
