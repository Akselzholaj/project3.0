import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Guest {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String begin;
    private String end;
    private int guests;
    private int tableNumber;

    public Guest(int id, String name, String email, String phone, String begin, String end, int guests, int tableNumber) {
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

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public int getGuests() {
        return guests;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    // Method to insert a guest
    public void insertGuest() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "INSERT INTO guests (name, email, phone, begin, end, guests, table_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, getName());
                preparedStatement.setString(2, getEmail());
                preparedStatement.setString(3, getPhone());
                preparedStatement.setString(4, getBegin());
                preparedStatement.setString(5, getEnd());
                preparedStatement.setInt(6, getGuests());
                preparedStatement.setInt(7, getTableNumber());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update a guest
    public void updateGuest() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "UPDATE guests SET name = ?, email = ?, phone = ?, begin = ?, end = ?, guests = ?, table_number = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, getName());
                preparedStatement.setString(2, getEmail());
                preparedStatement.setString(3, getPhone());
                preparedStatement.setString(4, getBegin());
                preparedStatement.setString(5, getEnd());
                preparedStatement.setInt(6, getGuests());
                preparedStatement.setInt(7, getTableNumber());
                preparedStatement.setInt(8, getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to delete a guest
    public void deleteGuest() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "DELETE FROM guests WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve all guests
    public static List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "SELECT * FROM guests";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String email = resultSet.getString("email");
                    String phone = resultSet.getString("phone");
                    String begin = resultSet.getString("begin");
                    String end = resultSet.getString("end");
                    int guestsNumber = resultSet.getInt("guests");
                    int tableNumber = resultSet.getInt("table_number");
                    guests.add(new Guest(id, name, email, phone, begin, end, guestsNumber, tableNumber));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guests;
    }
}
