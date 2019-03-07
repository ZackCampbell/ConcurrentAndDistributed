package HW3;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;

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
        String CustomerName;
        String CarName;
        String CarColor;

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
                    CustomerName = st.next();
                    CarName = st.next();
                    CarColor = st.next();
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
                    else {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < custList.size(); i++) {
                            if (i == custList.size() - 1) {
                                sb.append(custList.get(i));
                            } else {
                                sb.append(custList.get(i) + "#&");
                            }
                        }
                        pout.println(sb.toString());
                    }
                } else if (tag.equals("inventory")) {
                    ArrayList<CarInventory.CarEntry> inventory = carInventory.getInventory();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < inventory.size(); i++) {
                        if (i == inventory.size() - 1) {
                            sb.append(inventory.get(i).brand + " " + inventory.get(i).color + " " + inventory.get(i).quantity);
                        } else {
                            sb.append(inventory.get(i).brand + " " + inventory.get(i).color + " " + inventory.get(i).quantity + "#&");
                        }
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
            for (int i = 0; i < carInventory.getInventory().size(); i++) {
                String temp;
                if (i == carInventory.getInventory().size() - 1) {
                    temp = carInventory.getInventory().get(i).toString();
                } else {
                    temp = carInventory.getInventory().get(i).toString() + "\n";
                }
                writer.append(temp);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
