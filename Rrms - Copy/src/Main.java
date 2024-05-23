import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin");
                    JFrame frame = new JFrame("Restaurant Reservation System");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(800, 600);
                    frame.setLocationRelativeTo(null); // Center the frame on the screen
                    frame.add(new Resturant(connection));
                    frame.setVisible(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
