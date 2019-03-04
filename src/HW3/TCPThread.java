package HW3;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPThread extends Thread {
    private Socket clientSocket;
    private CarInventory carInventory;
    private RentalRecords rentalRecords;

    public TCPThread(Socket s, CarInventory carInventory, RentalRecords records){
        this.clientSocket = s;
        this.carInventory = carInventory;
        this.rentalRecords = records;
    }

    public void run() {

        Scanner sc;
        try {
            sc = new Scanner(clientSocket.getInputStream());
            PrintStream pout = new PrintStream(clientSocket.getOutputStream());
            boolean running = true;
            while (running) {
                String command = sc.nextLine();
                Scanner st = new Scanner(command);
                String tag = st.next();

                if (tag.equals("rent")) {
                    String CustomerName = st.next();
                    String CarName = st.next();
                    String CarColor = st.next();
                    String searchResults = carInventory.search(CarName, CarColor);
                    if (searchResults.equals("NotAvailable")) {
                        pout.println("Request Failed - Car not available");
                    } else if (searchResults.equals("NoCar")) {
                        pout.println("Request Failed - We do not have this car");
                    } else {
                        carInventory.rentCar(CarName, CarColor);
                        int recNumber = rentalRecords.insert(CustomerName, CarName, CarColor);
                        pout.println("Your request has been approved, " + recNumber + " " + CustomerName
                                + " " + CarName + " " + CarColor);
                    }
                } else if (tag.equals("return")) {
                    int recNum = st.nextInt();
                    ArrayList<String> brandAndColor = rentalRecords.remove(recNum);
                    if (!brandAndColor.isEmpty()) {
                        carInventory.returnCar(brandAndColor.get(0), brandAndColor.get(1));
                        pout.println(recNum + " is returned");
                    } else {
                        pout.println("NO SUCH CAR TO RETURN");
                    }
                } else if (tag.equals("list")) {
                    String custName = st.next();
                    ArrayList<String> custList = rentalRecords.getList(custName);
                    if (custList.isEmpty())
                        pout.println("No record found for " + custName);
                    else
                        for (String s : custList) {
                            pout.println(s);
                        }
                } else if (tag.equals("inventory")) {
                    ArrayList<CarInventory.CarEntry> inventory = carInventory.getInventory();
                    StringBuilder sb = new StringBuilder();
                    for (CarInventory.CarEntry c : inventory) {
                        sb.append(c.brand + " " + c.color + " " + c.quantity + "#&");
                    }
                    pout.println(sb.toString());
                } else if (tag.equals("exit")) {
                    running = false;

                }
                if (running)
                    pout.flush();
            }
            clientSocket.close();
            String currentDir = new File(".").getCanonicalPath();
            FileWriter writer = new FileWriter(currentDir + "/src/HW3/inventory.txt", false);
            for (CarInventory.CarEntry s : carInventory.getInventory()) {
                String temp = s.toString() + "\n";
                writer.append(temp);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
