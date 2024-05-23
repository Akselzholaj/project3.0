import java.sql.Timestamp;

public class Reservation {
    private int id;
    private String name;
    private String email;
    private String phone;
    private Timestamp begin; // Corrected data type
    private Timestamp end; // Corrected data type
    private int guests;
    private int tableNumber;

    public Reservation(int id, String name, String email, String phone, Timestamp begin, Timestamp end, int guests, int tableNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.begin = begin;
        this.end = end;
        this.guests = guests;
        this.tableNumber = tableNumber;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Timestamp getBegin() {
        return begin;
    }

    public Timestamp getEnd() {
        return end;
    }

    public int getGuestsCount() { // Corrected method name
        return guests;
    }

    public int getTableNumber() {
        return tableNumber;
    }
}
