package HW3;

import javax.naming.ldap.SortKey;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPThread extends Thread {
    Socket s;
    CarInventory carInventory;

    public TCPThread(Socket s, CarInventory carInventory){
        this.s = s;
        this.carInventory = carInventory;
    }

    public void run() {

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
                    String CustomerName = st.nextLine();
                    String CarName = st.next();
                    String CarColor = st.next();

                    if (carInventory.search(CarName, CarColor).equals("NotAvailable"))
                        System.out.println("Request Failed - Car not available");
                    else if (carInventory.search(CarName, CarColor).equals("NoCar"))
                        System.out.println("Request Failed - We do not have this car");
                    else
                        System.out.println("Request go through"); // need to assign update record information
                } else if (tag.equals("return")) {
                    System.out.println("Request go through"); // need to return a record ID #
                } else if (tag.equals("list")) {
                    System.out.println("Request go through"); // need to return a list of car that is rented by the customer
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
    }
}
