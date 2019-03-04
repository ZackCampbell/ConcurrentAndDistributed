package HW3;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class UDPThread implements Callable<String> {

    CarInventory carInventory;
    DatagramPacket datapacket;
    ArrayList<CarServer.RentalRecord> records;

    public UDPThread(CarInventory carInventory, DatagramPacket datapacket, ArrayList<CarServer.RentalRecord> records) {
        this.carInventory = carInventory;
        this.datapacket = datapacket;
        this.records = records;
    }

    public String call() {
        String message = "";
        String CustomerName;
        String CarName;
        String CarColor;
        int RecordNum;

        String command = new String(datapacket.getData(), 0, datapacket.getLength());
        System.out.println("received:" + command);
        String[] tokens = command.split(" ");
        String tag = tokens[0];

        if (tag.equals("rent")) {
            CustomerName = tokens[1];
            CarName = tokens[2];
            CarColor = tokens[3];

            if (carInventory.search(CarName, CarColor).equals("NotAvailable"))
                message = "Request Failed - Car not available";
            else if (carInventory.search(CarName, CarColor).equals("NoCar"))
                message = "Request Failed - We do not have this car";
            else {
                message = "Your request has been approved " + "RecordID " + CustomerName + " " + CarName + " " + CarColor;
                records.add(new CarServer.RentalRecord(1, CustomerName, CarName, CarColor));
            }

        } else if (tag.equals("return")) {
            int count = 0;
            RecordNum = Integer.parseInt(tokens[1]);
            for (CarServer.RentalRecord c : records) {
                if (c.recordNum == RecordNum) {
                    message = c.recordNum + " is returned’";
                    carInventory.returnCar(c.brand, c.color);
                    break;
                }
                count++;
            }

            if (count == records.size()) {
                message = RecordNum + " not found, no such rental record";
            }

        } else if (tag.equals("list")) {
            int count = 0;
            CustomerName = tokens[1];

            for (CarServer.RentalRecord c : records) {
                if (c.name.equals(CustomerName)) {
                    message = c.recordNum + " " + c.brand + " " + c.color;
                    break;
                }
                count++;
            }

            if (count == records.size()) {
                message = "No record found for" + CustomerName;
            }

        } else if (tag.equals(("inventory"))) {
            ArrayList<CarInventory.CarEntry> inventory = carInventory.getInventory();
            for (CarInventory.CarEntry c : inventory) {
                message = c.brand + " " + c.color + " " + c.quantity;
            }

        } else if (tag.equals("exit")){

        }
            return message;
    }


}
