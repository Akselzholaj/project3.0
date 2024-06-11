import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin");
                JFrame frame = new JFrame("Restaurant Reservation System");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null); // Center the frame on the screen

                // Create a panel for the image and reserve button
                JPanel welcomePanel = new JPanel(new BorderLayout());

                // Add an image to the panel
                ImageIcon imageIcon = createImageIcon("C:\\Users\\aksel\\Desktop\\Rrms - Copy\\Rrms - Copy\\src\\image.jpg"); // Replace "picture.jpg" with your image path
                if (imageIcon != null) {
                    JLabel imageLabel = new JLabel(imageIcon);
                    welcomePanel.add(imageLabel, BorderLayout.CENTER);
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Image file not found", "Image Loading Error", JOptionPane.ERROR_MESSAGE);
                }

                // Add a reserve button
                JButton reserveButton = new JButton("Reserve");
                reserveButton.addActionListener(e -> {
                    // Action when reserve button is clicked
                    showRestaurantLayout(frame, connection);
                });
                welcomePanel.add(reserveButton, BorderLayout.SOUTH);

                frame.add(welcomePanel);
                frame.setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void showRestaurantLayout(JFrame frame, Connection connection) {
        frame.getContentPane().removeAll(); // Remove existing components from the frame
        frame.add(new Resturant(connection)); // Add the restaurant layout panel
        frame.revalidate(); // Revalidate the frame
    }

    private static ImageIcon createImageIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            return icon;
        } else {
            return null;
        }
    }
}
