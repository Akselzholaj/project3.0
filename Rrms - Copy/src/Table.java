import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private int tableNumber;
    private String guestName;
    private String beginTime;
    private String endTime;
    private int numberOfGuests;
    private int reservationCount;

    public Table(int tableNumber, String guestName, String beginTime, String endTime, int numberOfGuests) {
        this.tableNumber = tableNumber;
        this.guestName = guestName;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.numberOfGuests = numberOfGuests;
        this.reservationCount = 0; // Initialize reservation count to zero
    }

    // Getters and setters
    public int getTableNumber() {
        return tableNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public int getReservationCount() {
        return reservationCount;
    }

    // Method to increment reservation count
    public void incrementReservationCount() {
        this.reservationCount++;
    }

    // Method to check if a table is fully booked
    public boolean isFullyBooked() {
        return reservationCount >= 5;
    }

    // Method to insert a reservation into the database
    public void insertReservation() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "INSERT INTO tables (table_number, guest_name, begin_time, end_time, number_of_guests) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, tableNumber);
                preparedStatement.setString(2, guestName);
                preparedStatement.setString(3, beginTime);
                preparedStatement.setString(4, endTime);
                preparedStatement.setInt(5, numberOfGuests);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Table reservation inserted successfully.");
                    incrementReservationCount(); // Increment reservation count locally
                } else {
                    System.out.println("Failed to insert table reservation.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update a reservation in the database
    public void updateReservation() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "UPDATE tables SET guest_name = ?, begin_time = ?, end_time = ?, number_of_guests = ? WHERE table_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, guestName);
                preparedStatement.setString(2, beginTime);
                preparedStatement.setString(3, endTime);
                preparedStatement.setInt(4, numberOfGuests);
                preparedStatement.setInt(5, tableNumber);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Table reservation updated successfully.");
                } else {
                    System.out.println("Failed to update table reservation.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve all reservations for a specific table
    public static List<Table> getAllReservations() {
        List<Table> reservations = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "SELECT * FROM tables";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int tableNumber = resultSet.getInt("table_number");
                    String guestName = resultSet.getString("guest_name");
                    String beginTime = resultSet.getString("begin_time");
                    String endTime = resultSet.getString("end_time");
                    int numberOfGuests = resultSet.getInt("number_of_guests");
                    int reservationCount = resultSet.getInt("reservation_count"); // Assuming there is a column named reservation_count
                    reservations.add(new Table(tableNumber, guestName, beginTime, endTime, numberOfGuests));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
}
