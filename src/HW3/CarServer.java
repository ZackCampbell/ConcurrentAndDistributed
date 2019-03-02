package HW3;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

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

    public static void parseInventoryFile(String fileName) {
        try {
            Scanner sc = new Scanner(new FileReader(fileName));
            while (sc.hasNextLine()) {



                System.out.println(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

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

        parseInventoryFile(fileName);
        // TODO: handle request from clients
    }
}
