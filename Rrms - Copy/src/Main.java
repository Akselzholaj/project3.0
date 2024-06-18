import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Connection connection =  DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/sakila", "root", "admin");
                //Trigger creation
                //Table table = new Table();
                //table.createTrigger();

                JFrame frame = new JFrame("Restaurant Reservation System");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1024, 800);
                frame.setLocationRelativeTo(null); // Center the frame on the screen

                frame.setMinimumSize(new Dimension(800, 600));
                frame.setMaximumSize(new Dimension(1024, 1024));
                frame.setMaximumSize(frame.getPreferredSize());

                frame.setResizable(false);

                // Create a panel for the image and reserve button
                JPanel welcomePanel = new JPanel(new BorderLayout());

                // Add an image to the panel
                ImageIcon imageIcon = createImageIcon("C:\\Users\\aksel\\Desktop\\Rrms - Copy\\Rrms - Copy\\src\\tab.jpeg"); // Replace "picture.jpg" with your image path
                if (imageIcon != null) {
                    JLabel imageLabel = new JLabel(imageIcon);
                    welcomePanel.setLayout(new GridBagLayout());
                    welcomePanel.add(imageLabel);
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Image file not found", "Image Loading Error", JOptionPane.ERROR_MESSAGE);
                }

                // Add a reserve button
                JButton reserveButton = new JButton("Reserve");
                reserveButton.setFont(new Font("Times New Roman", Font.BOLD, 30));
                reserveButton.setBackground(new Color(196, 164, 132));
                reserveButton.setForeground(new Color(74, 61, 61));
                reserveButton.setFocusPainted(false);

                reserveButton.setPreferredSize(new Dimension(100, 100));

                reserveButton.addActionListener(e -> {
                    reserveButton.setText("Loading...");
                    SwingUtilities.invokeLater(()->{
                        // Action when reserve button is clicked
                        showRestaurantLayout(frame, connection);
                        reserveButton.setText("Reserve");
                    });
                });
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                welcomePanel.add(reserveButton, gbc);

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