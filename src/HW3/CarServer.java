package HW3;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;

public class CarServer {
    class RentalRecord {
        int recordNum;
        String name, brand, color;
        public RentalRecord(int num, String name, String bName, String cName) {
            this.recordNum = num;
            this.name = name;
            this.brand = bName;
            this.color = cName;
        }
    }

    ArrayList<RentalRecord> records = new ArrayList<>();

    private static final int len = 1024;

    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        CarInventory inv = new CarInventory();
        try {
            Scanner sc = new Scanner(new FileReader(fileName));
            while (sc.hasNextLine()) {
                String entry = sc.nextLine();
                String[] tokens = entry.split(" ");
                inv.insert(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
            }
            System.out.println(inv.getInventory());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();

        try {
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            DatagramPacket datapacket, returnpacket;
            byte[] buf = new byte[len];
            while (true) {
                while ((s = listener.accept()) != null) {
                    threadPool.submit(new TCPThread(s, inv));
                }
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);
                Future<String> retString = threadPool.submit(new UDPThread(inv));
                returnpacket = new DatagramPacket(
                        retString.get().getBytes(),
                        datapacket.getLength(),
                        datapacket.getAddress(),
                        datapacket.getPort());
                datasocket.send(returnpacket);
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Server aborted:" + e);
        }

        // TODO: handle request from clients
    }
}
