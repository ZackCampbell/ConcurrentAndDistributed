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
    Map<Integer, Integer> seqMap;
    int seq = -1;
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
        this.storageMap = new ConcurrentHashMap<>();
        this.seqMap = new ConcurrentHashMap<>();
        this.totalPeers = peers.length;
        this.minimum = new AtomicInteger(-1);
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
        if (!seqMap.containsValue(seq))
            mutex.lock();
            this.seq = seq;
            Storage storage = new Storage(value);
            Thread newInstance = new Thread(this);
            seqMap.put((int)newInstance.getId(), this.seq);
            storageMap.put(this.seq, storage);
            newInstance.start();
            mutex.unlock();
    }

    @Override
    public void run(){
        int seq = seqMap.get((int)Thread.currentThread().getId());
        Storage storage = storageMap.get(seq);
        while (storage.state != State.Decided) {
            storage.mutex.lock();
            storage.n = storage.n_p + this.me;
            storage.n_prime = 0;
            storage.mutex.unlock();
            ArrayList<Response> accResponseList = new ArrayList<>();
            ArrayList<Response> prepResponseList = new ArrayList<>();
            int prepCounter = 0;
            for (int id = 0; id < this.ports.length; id++) {                               // Send "Prepare" request to all peers
                Response prepResponse;
                if (id != this.me)
                    prepResponse = this.Call("Prepare", new Request(storage.n, storage.v, seq), id);
                else
                    prepResponse = Prepare(new Request(storage.n, storage.v, seq));
                if (prepResponse != null && !prepResponse.getReject()) {
                    doneList[id] = prepResponse.getDone();
                    prepResponseList.add(prepResponse);
                    prepCounter++;
                }
            }
            for (Response r : prepResponseList) {
                if (r.getNA() > storage.n_prime) {
                    storage.mutex.lock();
                    storage.n_prime = r.getNA();
                    storage.v_prime = r.getVA();
                    storage.mutex.unlock();
                }
            }
//            storage.setHighestPromise(currHighestProposal);
            if (prepCounter > peers.length / 2) {
                if (storage.n_prime < storage.n) {
                    storage.mutex.lock();
                    storage.v_prime = storage.v;
                    storage.mutex.unlock();
                }
                int accCounter = 0;
                for (int id = 0; id < this.ports.length; id++) {                           // Send "Accept" request to all peers
                    Response accResponse;
                    if (id != this.me)
                        accResponse = this.Call("Accept", new Request(storage.n, storage.v_prime, seq), id);
                    else
                        accResponse = Accept(new Request(storage.n, storage.v_prime, seq));
                    if (accResponse != null && !accResponse.getReject()) {
                        doneList[id] = accResponse.getDone();
                        accResponseList.add(accResponse);
                    }
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
                        if (id != this.me)
                            decResponse = this.Call("Decide", new Request(storage.v, seq), id);
                        else
                            decResponse = Decide(new Request(storage.v, seq));
                        if (decResponse != null) {
                            doneList[id] = decResponse.getDone();
                        }
                    }
                }
            }
        }
    }


    // RMI handler
    public Response Prepare(Request req){
        Storage storage = storageMap.get(req.getSeq());
        if (storage == null) {
            storage = new Storage(req.getV());
            storage.n_p = req.getN();
            storageMap.put(req.getSeq(), storage);
            return new Response(req.getN(), storage.n_a, storage.v_prime, this.minimum.get());
        } else if (req.getN() >= storage.n_p) {
            storage.n_p = req.getN();
            return new Response(req.getN(), storage.n_a, storage.v_a, this.minimum.get());
        } else {
            return new Response(this.minimum.get());
        }
    }

    public Response Accept(Request req){
        Response response;
        try {
            Storage storage = getStorage(req);
            if (req.getN() >= storage.n_p) {
                storage.n_p = req.getN();
                storage.n_a = req.getN();
                storage.v_a = req.getV();
                response = new Response(req.getN(), this.minimum.get());
            } else {
                response = new Response(this.minimum.get());
            }
        } catch (InterruptedException e) {
            response = null;
        }
        return response;
    }

    private Storage getStorage(Request req) throws InterruptedException {
        Storage storage = storageMap.get(req.getSeq());
        while (storage == null) {
            Thread.sleep(100);
            storage = storageMap.get(req.getSeq());
        }
        return storage;
    }

    public Response Decide(Request req){
        try {
            Storage storage = getStorage(req);
            storage.state = State.Decided;
            storage.retValue = req.getV();
        } catch (InterruptedException e) {}
        return new Response(req.getV(), this.minimum.get(), true);
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
            minimum.set(i);
        }
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

    public Response getMinimum() {
        return new Response(this.minimum.get());
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
        return forget() + 1;
    }

    public int forget(){
        int min = 0;
        for (int id = 0; id < peers.length; id++) {
            if (id == this.me)
                min = Math.min(min, getMinimum().getDone());
            else
                min = Math.min(min, doneList[id]);
        }
        for (int key : storageMap.keySet()) {
            if (key < min)
                storageMap.remove(min);
        }
        for (int i : seqMap.keySet()) {
            if (seqMap.get(i) < min) {
                seqMap.remove(i);
            }
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
        Storage storage = storageMap.get(seq);
        if (seq < minimum.get() && storage == null) {
            return new retStatus(State.Forgotten, null);
        } else if (storage == null && seq >= minimum.get()) {
            return new retStatus(State.Pending, null);
        }
        return new retStatus(storage.state, storage.retValue);


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
        ReentrantLock mutex;
        int n;
        int n_prime;
        int n_p = -1;
        int n_a = 0;
        State state;
        Object v_a = null;
        Object v;
        Object v_prime;
        Object retValue;

        public Storage(Object v) {
            this.mutex = new ReentrantLock();
            this.state = State.Pending;
            this.v = v;
        }
    }

}
