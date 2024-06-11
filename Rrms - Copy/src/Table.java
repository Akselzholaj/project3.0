import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Table {
    private int tableNumber;
    private String customerName;
    private String beginTime;
    private String endTime;
    private int numberOfGuests;
    private AtomicInteger reservationCounter;

    public Table(int tableNumber, String customerName, String beginTime, String endTime, int numberOfGuests) {
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.numberOfGuests = numberOfGuests;
        this.reservationCounter = new AtomicInteger(0);
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
            if (isTableAvailable(connection, getTableNumber(), getBeginTime(), getEndTime())) {
                String query = "INSERT INTO tables (table_number, customer_name, begin_time, end_time, number_of_guests) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setInt(1, getTableNumber());
                    preparedStatement.setString(2, getCustomerName());
                    preparedStatement.setString(3, getBeginTime());
                    preparedStatement.setString(4, getEndTime());
                    preparedStatement.setInt(5, getNumberOfGuests());
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Table reservation inserted successfully.");
                        updateReservationCounter(connection, getTableNumber(), 1);
                    } else {
                        System.out.println("Failed to insert table reservation.");
                    }
                }
            } else {
                System.out.println("Table " + getTableNumber() + " is already reserved during the specified time range.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to remove a table reservation
    public void removeTableReservation(int reservationId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "DELETE FROM tables WHERE reservation_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, reservationId);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Table reservation removed successfully.");
                    updateReservationCounter(connection, getTableNumber(), -1);
                } else {
                    System.out.println("Failed to remove table reservation.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to check if the table is available during the specified time range
    private boolean isTableAvailable(Connection connection, int tableNumber, String beginTime, String endTime) {
        try {
            String query = "SELECT * FROM tables WHERE table_number = ? AND ((begin_time >= ? AND begin_time < ?) OR (end_time > ? AND end_time <= ?))";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, tableNumber);
                preparedStatement.setString(2, beginTime);
                preparedStatement.setString(3, endTime);
                preparedStatement.setString(4, beginTime);
                preparedStatement.setString(5, endTime);
                ResultSet resultSet = preparedStatement.executeQuery();
                return !resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to update the reservation counter
    private void updateReservationCounter(Connection connection, int tableNumber, int increment) {
        try {
            String query = "UPDATE tables SET reservation_count = reservation_count + ? WHERE table_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, increment);
                preparedStatement.setInt(2, tableNumber);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    String checkQuery = "SELECT reservation_count FROM tables WHERE table_number = ?";
                    try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                        checkStatement.setInt(1, tableNumber);
                        ResultSet resultSet = checkStatement.executeQuery();
                        if (resultSet.next()) {
                            int newCount = resultSet.getInt("reservation_count");
                            if (newCount >= 5) {
                                removeFromLayout(connection, tableNumber);
                            } else if (newCount < 5 && increment < 0) {
                                reAddToLayout(connection, tableNumber);
                            }
                        }
                    }
                } else {
                    System.out.println("Failed to update reservation count for table " + tableNumber);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to remove the table from layout
    private void removeFromLayout(Connection connection, int tableNumber) {
        try {
            String query = "DELETE FROM tables WHERE table_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, tableNumber);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Table " + tableNumber + " marked as fully reserved and removed from layout.");
                } else {
                    System.out.println("Failed to remove table " + tableNumber + " from layout.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to re-add the table to layout
    private void reAddToLayout(Connection connection, int tableNumber) {
        System.out.println("Table " + tableNumber + " re-added to layout.");
    }

    // Method to get all reservations
    public static List<Table> getAllReservations() {
        List<Table> reservations = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "SELECT * FROM tables";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int tableNumber = resultSet.getInt("table_number");
                    String customerName = resultSet.getString("customer_name");
                    String beginTime = resultSet.getString("begin_time");
                    String endTime = resultSet.getString("end_time");
                    int numberOfGuests = resultSet.getInt("number_of_guests");
                    reservations.add(new Table(tableNumber, customerName, beginTime, endTime, numberOfGuests));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
}
