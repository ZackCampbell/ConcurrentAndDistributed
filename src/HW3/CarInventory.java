package HW3;

import java.util.ArrayList;

public class CarInventory {
    class CarEntry {
        public String brand;
        public String color;
        public int quantity;
        public CarEntry(String brandName, String colorName, int q) {
            this.brand = brandName;
            this.color = colorName;
            this.quantity = q;
        }
        ArrayList<CarEntry> inventory = new ArrayList<>();
        public synchronized String search(String brandName, String colorName) {
            for (CarEntry c : inventory) {
                if (c.brand.equals(brandName) && c.color.equals(colorName) && c.quantity == 0) {
                    return "NotAvailable";
                } else if (c.brand.equals(brandName) && c.color.equals(colorName) && c.quantity > 0) {
                    return "Available";
                }
            }
            return "NoCar";
        }

        public synchronized void returnCar(String bName, String cName) {
            for (CarEntry c : inventory) {
                if (c.brand.equals(bName) && c.color.equals(cName)) {
                    c.quantity++;
                    return;
                }
            }
        }

        public synchronized void rentCar(String bName, String cName) {
            for (CarEntry c : inventory) {
                if (c.brand.equals(bName) && c.color.equals(cName)) {
                    c.quantity--;
                    return;
                }
            }
        }

        public synchronized void insert(String brandName, String colorName, int q) {
            inventory.add(new CarEntry(brandName, colorName, q));
            notifyAll();
        }

        public synchronized ArrayList<CarEntry> getInventory() {
            return inventory;
        }

        public synchronized void clear() {
            inventory.clear();
        }

    }
}
