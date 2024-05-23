import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private int tableNumber;
    private String customerName;
    private String beginTime;
    private String endTime;
    private int numberOfGuests;

    public Table(int tableNumber, String customerName, String beginTime, String endTime, int numberOfGuests) {
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.numberOfGuests = numberOfGuests;
    }

    // Getters and setters
    public int getTableNumber() {
        return tableNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    // Method to insert the table reservation into the database
    public void insertTableReservation() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "INSERT INTO tables (table_number, costumer_name, begin_time, end_time, number_of_guests) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, getTableNumber());
                preparedStatement.setString(2, getCustomerName());
                preparedStatement.setString(3, getBeginTime());
                preparedStatement.setString(4, getEndTime());
                preparedStatement.setInt(5, getNumberOfGuests());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update the table reservation in the database
    public void updateTableReservation() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "UPDATE tables SET custumer_name = ?, begin_time = ?, end_time = ?, number_of_guests = ? WHERE table_number = ? AND begin_time = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, getCustomerName());
                preparedStatement.setString(2, getBeginTime());
                preparedStatement.setString(3, getEndTime());
                preparedStatement.setInt(4, getNumberOfGuests());
                preparedStatement.setInt(5, getTableNumber());
                preparedStatement.setString(6, getBeginTime());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to delete the table reservation from the database
    public void deleteTableReservation() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "DELETE FROM tables WHERE table_number = ? AND begin_time = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, getTableNumber());
                preparedStatement.setString(2, getBeginTime());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve all table reservations
    public static List<Table> getAllTableReservations() {
        List<Table> tables = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "SELECT * FROM tables";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int tableNumber = resultSet.getInt("table_number");
                    String customerName = resultSet.getString("custumer_name");
                    String beginTime = resultSet.getString("begin_time");
                    String endTime = resultSet.getString("end_time");
                    int numberOfGuests = resultSet.getInt("number_of_guests");
                    tables.add(new Table(tableNumber, customerName, beginTime, endTime, numberOfGuests));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }
}
