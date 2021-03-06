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

        int len = 4096;

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
            Socket serverTCPSocket = new Socket(hostAddress, tcpPort);
            PrintStream tcpOutput = new PrintStream(serverTCPSocket.getOutputStream());
            Scanner tcpReturn = new Scanner(serverTCPSocket.getInputStream());
            DatagramSocket clientUDPSocket = new DatagramSocket();
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
//                System.out.println(cmd);
                String[] tokens = cmd.split(" ");
                String retString;
                if (tokens[0].equals("setmode")) {
                    mode = tokens[1];
                    if (mode.equals("U")) {
                        tcpOutput.close();
                        tcpReturn.close();
                        serverTCPSocket.close();
                    }
                } else if (tokens[0].equals("rent")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    retStringList.add(retString);
                } else if (tokens[0].equals("return")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    retStringList.add(retString);
                } else if (tokens[0].equals("inventory")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                        retString = retString.replaceAll("#&", "\n");
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    retStringList.add(retString);
                } else if (tokens[0].equals("list")) {
                    if (mode.equals("T")) {
                        retString = sr_TCP(tcpOutput, tcpReturn, cmd);
                        retString = retString.replaceAll("#&", "\n");
                    } else {
                        retString = sr_UDP(udpPort, len, ia, clientUDPSocket, cmd);
                    }
                    retStringList.add(retString);
                } else if (tokens[0].equals("exit")) {
                    if (mode.equals("T")) {
                        tcpOutput.println(cmd);
                        tcpOutput.flush();
                        tcpOutput.close();
                        tcpReturn.close();
                        serverTCPSocket.close();
                    } else {
                        byte[] sBuffer;
                        DatagramPacket sPacket;
                        sBuffer = cmd.getBytes();
                        sPacket = new DatagramPacket(sBuffer, sBuffer.length, ia, udpPort);
                        clientUDPSocket.send(sPacket);
                    }
                    break;
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  Output to out_(id) file
        try {
            String currentDir = new File(".").getCanonicalPath();
            File outputFile = new File(currentDir + "/out_" + clientId + ".txt");
            FileWriter writer = new FileWriter(outputFile);
            for (int i = 0; i < retStringList.size(); i++) {
                String temp;
                if (i == retStringList.size() - 1) {
                    temp = retStringList.get(i);
                } else {
                    temp = retStringList.get(i) + "\n";
                }
                writer.append(temp);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String sr_UDP(int udpPort, int len, InetAddress ia, DatagramSocket clientUDPSocket, String cmd) throws IOException {
        byte[] sBuffer = cmd.getBytes();
        DatagramPacket sPacket, rPacket;
        sPacket = new DatagramPacket(sBuffer, sBuffer.length, ia, udpPort);
        clientUDPSocket.send(sPacket);
        byte[] rBuffer = new byte[len];
        rPacket = new DatagramPacket(rBuffer, len);
        clientUDPSocket.receive(rPacket);
        return new String(rPacket.getData(), 0, rPacket.getLength());
    }

    private static String sr_TCP(PrintStream tcpOutput, Scanner tcpReturn, String cmd) throws IOException {
        tcpOutput.println(cmd);
        tcpOutput.flush();
        return tcpReturn.nextLine();
    }
}
