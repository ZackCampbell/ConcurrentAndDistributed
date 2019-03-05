package HW3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class UDPThread implements Callable<String> {

    private CarInventory carInventory;
    private DatagramPacket datapacket;
    private RentalRecords records;

    public UDPThread(CarInventory carInventory, DatagramPacket datapacket, RentalRecords rentalRecords) {
        this.carInventory = carInventory;
        this.datapacket = datapacket;
        this.records = rentalRecords;
    }

    public String call() {
        String message = "";
        String customerName;
        String carName;
        String carColor;
        String command = new String(datapacket.getData(), 0, datapacket.getLength());
        String[] tokens = command.split(" ");
        String tag = tokens[0];

        if (tag.equals("rent")) {
            customerName = tokens[1];
            carName = tokens[2];
            carColor = tokens[3];
            String searchResults = carInventory.search(carName, carColor);
            if (searchResults.equals("NotAvailable"))
                message = "Request Failed - Car not available";
            else if (searchResults.equals("NoCar"))
                message = "Request Failed - We do not have this car";
            else {
                carInventory.rentCar(carName, carColor);
                int recordNum = records.insert(customerName, carName, carColor);
                message = "Your request has been approved, " + recordNum + " " + customerName + " " + carName + " " + carColor;
            }
        } else if (tag.equals("return")) {
            int recordNum = Integer.parseInt(tokens[1]);
            ArrayList<String> brandAndColor = records.remove(recordNum);
            if (!brandAndColor.isEmpty()) {
                carInventory.returnCar(brandAndColor.get(0), brandAndColor.get(1));
                message = recordNum + " is returned";
            } else {
                message = "ERROR: NO SUCH CAR TO RETURN";
            }
        } else if (tag.equals("list")) {
            customerName = tokens[1];
            ArrayList<String> custList = records.getList(customerName);
            if (custList.isEmpty())
                message = "No record found for " + customerName;
            else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < custList.size(); i++) {
                    if (i == custList.size() - 1) {
                        sb.append(custList.get(i));
                    } else {
                        sb.append(custList.get(i) + "\n");
                    }
                }
                message = sb.toString();
            }
        } else if (tag.equals(("inventory"))) {
            ArrayList<CarInventory.CarEntry> inventory = carInventory.getInventory();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < inventory.size(); i++) {
                if (i == inventory.size() - 1) {
                    sb.append(inventory.get(i).brand + " " + inventory.get(i).color + " " + inventory.get(i).quantity);
                } else {
                    sb.append(inventory.get(i).brand + " " + inventory.get(i).color + " " + inventory.get(i).quantity + "\n");
                }
            }
            message = sb.toString();
        } else if (tag.equals("exit")){

            try {
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
            message = "Exiting";
        }
        return message;
    }


}
