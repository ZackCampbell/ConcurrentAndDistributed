package HW3;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

public class CarServer {
    private static final int len = 4096;
    private static DatagramPacket datapacket, returnpacket;
    private static Socket s;
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
        RentalRecords rentalRecords = new RentalRecords();

        try {
            Scanner sc = new Scanner(new FileReader(fileName));
            while (sc.hasNextLine()) {
                String entry = sc.nextLine();
                String[] tokens = entry.split(" ");
                inv.insert(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
            }
            //System.out.println(inv.getInventory());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ExecutorService tcpThreadPool = Executors.newCachedThreadPool();
        ExecutorService udpThreadPool = Executors.newCachedThreadPool();

        try {
            ServerSocket listener = new ServerSocket(tcpPort);

            DatagramSocket datasocket = new DatagramSocket(udpPort);



            Thread tcpThread = new Thread(() -> {
                while (true) {
                    try {
                        while ((s = listener.accept()) != null) {
                            tcpThreadPool.submit(new TCPThread(s, inv, rentalRecords));
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread udpThread = new Thread(() -> {
                while (true) {
                    try {
                        byte[] buf = new byte[len];
                        datapacket = new DatagramPacket(buf, buf.length);
                        datasocket.receive(datapacket);
                        Future<String> retString = udpThreadPool.submit(new UDPThread(inv, datapacket, rentalRecords));
                        returnpacket = new DatagramPacket(
                                retString.get().getBytes(),
                                retString.get().getBytes().length,
                                datapacket.getAddress(),
                                datapacket.getPort());
                        datasocket.send(returnpacket);
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        System.err.println("Server aborted:" + e);
                    }

                }
            });

            tcpThread.start();
            udpThread.start();
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }
}
