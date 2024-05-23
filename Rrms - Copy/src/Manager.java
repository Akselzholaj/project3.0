import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Manager {
    private Connection connection;

    public Manager(Connection connection) {
        this.connection = connection;
    }

    // Method to check if the connection is open
    private boolean isConnectionOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to execute SQL query with connection check
    private ResultSet executeQuery(String sql, Object... params) throws SQLException {
        if (!isConnectionOpen()) {
            throw new SQLException("Connection is closed.");
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement.executeQuery();
    }

    public void addManager(String name, String password) {
        try {
            if (managerExists(name)) {
                // Manager with the same name already exists, delete the duplicate one
                deleteManager(name);
                System.out.println("Duplicate manager with name '" + name + "' deleted.");
            }

            String sql = "INSERT INTO manager (name, password) VALUES (?, ?)";
            executeUpdate(sql, name, password);
            System.out.println("Manager added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteManager(String name) {
        try {
            String sql = "DELETE FROM manager WHERE name = ?";
            executeUpdate(sql, name);
            System.out.println("Manager " + name + " deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean managerExists(String name) {
        try {
            String sql = "SELECT COUNT(*) FROM manager WHERE name = ?";
            ResultSet resultSet = executeQuery(sql, name);
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getAuthenticator(String name, String password) {
        try {
            String sql = "SELECT * FROM manager WHERE name = ? AND password = ?";
            ResultSet resultSet = executeQuery(sql, name, password);
            return resultSet.next(); // If there's a row, authentication is successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to execute SQL update with connection check
    private void executeUpdate(String sql, Object... params) throws SQLException {
        if (!isConnectionOpen()) {
            throw new SQLException("Connection is closed.");
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        statement.executeUpdate();
    }
}
//