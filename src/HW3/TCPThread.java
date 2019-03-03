package HW3;

import javax.naming.ldap.SortKey;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class TCPThread implements Callable<String> {
    Socket s;
    CarInventory carInventory;
    ArrayList<CarServer.RentalRecord> records;

    public TCPThread(Socket s, CarInventory carInventory, ArrayList<CarServer.RentalRecord> records){
        this.s = s;
        this.carInventory = carInventory;
        this.records = records;
    }

    public String call() {
        String CustomerName;
        String CarName;
        String CarColor;

        Scanner sc = null;
        try {
            sc = new Scanner(s.getInputStream());
            PrintWriter pout = new PrintWriter(s.getOutputStream());

            while(sc.hasNextLine()) {
                String command = sc.nextLine();
                System.out.println("received:" + command);
                Scanner st = new Scanner(command);
                String tag = st.next();

                if (tag.equals("rent")) {
                    CustomerName = st.nextLine();
                    CarName = st.next();
                    CarColor = st.next();

                    if (carInventory.search(CarName, CarColor).equals("NotAvailable"))
                        System.out.println("Request Failed - Car not available");
                    else if (carInventory.search(CarName, CarColor).equals("NoCar"))
                        System.out.println("Request Failed - We do not have this car");
                    else {
                        System.out.println("Your request has been approved " + "RecordID " + CustomerName + " " + CarName + " " + CarColor);
                        records.add(new CarServer.RentalRecord(1, CustomerName, CarName, CarColor ));
                    }
                } else if (tag.equals("return")) {
                    int count = 0;
                    int RecordNum = Integer.parseInt(st.next());
                    for(CarServer.RentalRecord c : records) {
                        if (c.recordNum == RecordNum){
                            System.out.println(c.recordNum + " is returned’");
                            carInventory.returnCar(c.brand, c.color);
                            break;
                        }
                        count++;
                    }

                    if(count == records.size()){
                        System.out.println(RecordNum + " not found, no such rental record");
                    }

                } else if (tag.equals("list")) {
                    int count = 0;
                    CustomerName = st.nextLine();

                    for(CarServer.RentalRecord c : records) {
                        if (c.name.equals(CustomerName)){
                            System.out.println(c.recordNum + " " + c.brand + " " + c.color);
                            break;
                        }
                        count++;
                    }

                    if(count == records.size()){
                        System.out.println("No record found for" + CustomerName);
                    }

                } else if (tag.equals("inventory")) {
                    ArrayList<CarInventory.CarEntry> inventory = carInventory.getInventory();
                    for (CarInventory.CarEntry c : inventory) {
                       System.out.println(c.brand + " " + c.color + " " + c.quantity);
                    }
                } else if (tag.equals("exit ")) {
                    System.out.println("Request go through");
                }
            }
            pout.flush();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Task Complete";
    }
}
