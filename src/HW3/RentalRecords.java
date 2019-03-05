package HW3;

import java.util.ArrayList;
import java.util.HashMap;

public class RentalRecords {
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
    private int recNumber = 0;
//    private HashMap<String, ArrayList<RentalRecord>> recordMap;
    private ArrayList<RentalRecord> records;

    public RentalRecords() {
//        recordMap = new HashMap<>();
        records = new ArrayList<>();
    }

    public synchronized ArrayList<RentalRecord> getRecords() {
        return this.records;
    }

    public synchronized int insert(String name, String brand, String color) {
        records.add(new RentalRecord(++recNumber, name, brand, color));
        return recNumber;
    }

    public synchronized ArrayList<String> remove(int recNumber) {
        int index = -1;
        ArrayList<String> retList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).recordNum == recNumber) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            retList.add(records.get(index).brand);
            retList.add(records.get(index).color);
            records.remove(index);
            return retList;                // Found and removed record
        } else {
            return retList;               // Did not find the record
        }
    }

    public synchronized ArrayList<String> getList(String name) {
        ArrayList<String> retList = new ArrayList<>();
        for (RentalRecord r : records) {
            if (r.name.equals(name)) {
                retList.add(r.recordNum + " " + r.brand + " " + r.color);
            }
        }
        return retList;
    }

}
