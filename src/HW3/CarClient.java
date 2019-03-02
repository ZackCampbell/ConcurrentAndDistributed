package HW3;

import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;
public class CarClient {

    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;

        int len = 1024;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000;     // hardcoded -- must match the server's tcp port
        udpPort = 8000;     // hardcoded -- must match the server's udp port

        String mode = "U";
        ArrayList<String> retStringList = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new FileReader(commandFile));
            InetAddress ia = InetAddress.getByName(hostAddress);
            Socket clientTCPSocket = new Socket(hostAddress, tcpPort);
            DataOutputStream tcpOutput = new DataOutputStream(clientTCPSocket.getOutputStream());
            Scanner tcpReturn = new Scanner(clientTCPSocket.getInputStream());
            DatagramSocket clientUDPSocket = new DatagramSocket();
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                String retString;
                if (tokens[0].equals("setmode")) {
                    mode = tokens[1];
                    if (mode.equals("U")) {
                        tcpOutput.close();
                        tcpReturn.close();
                        clientTCPSocket.close();
                    }
                } else if (tokens[0].equals("rent")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    System.out.println(retString);
                    retStringList.add(retString);
                } else if (tokens[0].equals("return")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    System.out.println(retString);
                    retStringList.add(retString);
                } else if (tokens[0].equals("inventory")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    System.out.println(retString);
                    retStringList.add(retString);
                } else if (tokens[0].equals("list")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    System.out.println(retString);
                    retStringList.add(retString);
                } else if (tokens[0].equals("exit")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                        tcpOutput.close();
                        tcpReturn.close();
                        clientTCPSocket.close();
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    System.out.println(retString);
                    retStringList.add(retString);
                    break;
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String currentDir = new File(".").getCanonicalPath();
            File outputFile = new File(currentDir + "/out_" + clientId + ".txt");
            FileWriter writer = new FileWriter(outputFile);
            for (String s : retStringList) {
                String temp = s + "\n";
                writer.append(temp);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String sr_UDP(int udpPort, int len, InetAddress ia, DatagramSocket clientUDPSocket, String cmd) throws IOException {
        byte[] sBuffer;
        DatagramPacket sPacket;
        byte[] rBuffer;
        DatagramPacket rPacket;
        String retString;
        sBuffer = cmd.getBytes();
        sPacket = new DatagramPacket(sBuffer, sBuffer.length, ia, udpPort);
        clientUDPSocket.send(sPacket);
        rBuffer = new byte[len];
        rPacket = new DatagramPacket(rBuffer, rBuffer.length);
        clientUDPSocket.receive(rPacket);
        retString = new String(rPacket.getData(), 0, rPacket.getLength());
        return retString;
    }

    private static String sr_TCP(DataOutputStream tcpOutput, Scanner tcpReturn, String cmd) throws IOException {
        String retString;
        tcpOutput.writeUTF(cmd);
        tcpOutput.flush();
        retString = tcpReturn.nextLine();
        return retString;
    }
}
