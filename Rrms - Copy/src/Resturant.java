import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.io.IOException;


public class Resturant extends JPanel {

    private Connection connection;
    private Map<Integer, Integer> tableReservationCount;
    private JTable reservationTable; // Declaration of reservationTable
    //private JTable reservationTable;

    // colors
    private Color darkBackground = new Color(255, 255, 255);
    private Color darkForeground = Color.BLACK;
    private Color darkButtonBackground = new Color(196, 164, 132);
    private Color darkButtonForeground = Color.BLACK;

    public Resturant(Connection connection) {
        this.connection = connection;
        this.tableReservationCount = new HashMap<>();
        this.reservationTable = new JTable();
        setLayout(new BorderLayout());
        setBackground(darkBackground);

        JButton guestButton = createButton("New Reservation");
        JButton managerButton = createButton("Manager login");

        guestButton.addActionListener(e -> showGuestForm(0)); // Placeholder table number
        managerButton.addActionListener(e -> showManagerInterface());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(darkBackground);
        buttonPanel.add(guestButton);
        buttonPanel.add(managerButton);
        add(buttonPanel, BorderLayout.NORTH);

        createSeatingPlan();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(darkButtonBackground);
        button.setForeground(darkButtonForeground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(darkButtonBackground.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(darkButtonBackground);
            }
        });
        return button;
    }
    private void createSeatingPlan() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load background image
                try {
                    BufferedImage backgroundImage = ImageIO.read(getClass().getResource("bac.jpg"));
                    // Scale the background image to fit the panel
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // Handle image loading error
                }
            }
        };

        mainPanel.setBackground(darkBackground); // Set background color for mainPanel

        JPanel seatingPanel = new JPanel(new GridLayout(3, 6, 40, 30));
        seatingPanel.setBorder(BorderFactory.createEmptyBorder(18, 10, 10, 10));
        seatingPanel.setOpaque(false); // Make seating panel transparent

        // Load table images
        BufferedImage tableImage = null;
        try {
            tableImage = ImageIO.read(getClass().getResource("table.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading table image: " + ex.getMessage(), "Image Loading Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 1; i <= 18; i++) {
            int reservationCount = getReservationCountForTable(i);

            try {
                // Adjust image based on reservation count
                if (reservationCount >= 5) {
                    tableImage = ImageIO.read(getClass().getResource("booked.jpg"));
                } else {
                    tableImage = ImageIO.read(getClass().getResource("table.png"));
                }

                tableImage = getScaledCircularImage(tableImage, 140); // Change size here

                ImageIcon tableIcon = new ImageIcon(tableImage);
                JButton tableButton = new JButton(tableIcon);
                tableButton.setPreferredSize(new Dimension(140, 140)); // Change size here
                tableButton.setFocusPainted(false);
                tableButton.setContentAreaFilled(false);
                tableButton.setBorderPainted(false);
                tableButton.setForeground(darkForeground);
                tableButton.setHorizontalTextPosition(JButton.CENTER);
                tableButton.setVerticalTextPosition(JButton.CENTER);

                JLabel tableNumberLabel = new JLabel(Integer.toString(i));
                tableNumberLabel.setForeground(new Color(224, 224, 224)); // Set color of numbers
                tableNumberLabel.setHorizontalAlignment(JLabel.CENTER);
                tableNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 16)); // Example: Font size 16, bold

                JPanel tablePanel = new JPanel(new BorderLayout());
                tablePanel.setOpaque(false); // Make table panel transparent
                tablePanel.add(tableButton, BorderLayout.CENTER);
                tablePanel.add(tableNumberLabel, BorderLayout.NORTH);

                addTableButtonListener(tableButton, i);

                seatingPanel.add(tablePanel);

                // Disable button if table is fully reserved
                if (reservationCount >= 5) {
                    tableButton.setEnabled(false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating table: " + ex.getMessage(), "Table Creation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        mainPanel.add(seatingPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private int getReservationCountForTable(int tableNumber) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            String query = "SELECT reservation_count FROM tables WHERE table_number = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, tableNumber);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("reservation_count");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching reservation count: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0; // Default to 0 if an error occurs
    }


    private BufferedImage getScaledCircularImage(BufferedImage image, int size) {
        BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();

        // Calculate the scaling factor to fit the image into the circle
        double scaleFactor = Math.min(1.0 * size / image.getWidth(), 1.0 * size / image.getHeight());

        // Calculate new width and height for the scaled image
        int scaledWidth = (int) (image.getWidth() * scaleFactor);
        int scaledHeight = (int) (image.getHeight() * scaleFactor);

        // Calculate position to center the image in the resized circle
        int xPos = (size - scaledWidth) / 2;
        int yPos = (size - scaledHeight) / 2;

        // Create a clipping region for the circular shape
        Ellipse2D.Double clip = new Ellipse2D.Double(xPos, yPos, scaledWidth, scaledHeight);
        g2.setClip(clip);

        // Draw the scaled image centered within the circular shape
        g2.drawImage(image, xPos, yPos, scaledWidth, scaledHeight, null);

        // Add a border around the circular image
        g2.setColor(new Color(196, 164, 132));
        g2.setStroke(new BasicStroke(4)); // Adjust border thickness as needed
        g2.drawOval(xPos, yPos, scaledWidth, scaledHeight); // Draw oval border just inside the boundary

        g2.dispose();

        return resizedImage;
    }
    private void addTableButtonListener(JButton tableButton, int tableNumber) {
        tableButton.addActionListener(e -> {
            if (askForReservation(tableNumber)) {
                showGuestForm(tableNumber);
            } else {
                JOptionPane.showMessageDialog(null, "Reservation canceled for Table " + tableNumber);
            }
        });

        // Add hover effect to table buttons
        tableButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Scale button size slightly when hovered over
                tableButton.setPreferredSize(new Dimension(160, 160)); // Change hover size here
                tableButton.revalidate();
                tableButton.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Restore original button size when mouse exits
                tableButton.setPreferredSize(new Dimension(120, 120)); // Change original size here
                tableButton.revalidate();
                tableButton.repaint();
            }
        });
    }
    private boolean validateManager(String name, String password) {
        try {
            if (!connection.isValid(5)) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        Manager manager = new Manager(connection);
        return manager.getAuthenticator(name, password);
    }

    private boolean askForReservation(int tableNumber) {
        int option = JOptionPane.showConfirmDialog(null, "Do you want to reserve Table " + tableNumber + "?", "Reservation Confirmation", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }

    private void showGuestForm(int tableNumber) {
        JFrame guestFrame = new JFrame("Guest Details");
        guestFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        guestFrame.setSize(350, 250);
        guestFrame.setLocationRelativeTo(null);
        guestFrame.setBackground(darkBackground);
        guestFrame.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(8, 2));
        panel.setBackground(Color.white);

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField beginField = new JTextField();
        JTextField endField = new JTextField();
        JTextField guestsField = new JTextField();
        JTextField tableField = new JTextField(String.valueOf(tableNumber));
        JButton reserveButton = createButton("Reserve");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Begin(hh:mm):"));
        panel.add(beginField);
        panel.add(new JLabel("End(hh:mm):"));
        panel.add(endField);
        panel.add(new JLabel("Number of Guests:"));
        panel.add(guestsField);
        panel.add(new JLabel("Table Number:"));
        panel.add(tableField);
        panel.add(new JLabel(""));
        panel.add(reserveButton);

        reserveButton.addActionListener(e -> {
            if (validateInput(nameField.getText(), emailField.getText(), phoneField.getText(), beginField.getText(), endField.getText(), guestsField.getText())) {
                int numberOfGuests = Integer.parseInt(guestsField.getText());
                if (numberOfGuests > 6) {
                    JOptionPane.showMessageDialog(guestFrame, "The maximum number of guests per table is 6. Please reduce the number of guests.");
                    return;
                }
                // Check if the reservation falls within the allowed time period
                LocalTime beginTime = LocalTime.parse(beginField.getText());
                LocalTime endTime = LocalTime.parse(endField.getText());
                if (beginTime.isAfter(LocalTime.parse("12:59")) && endTime.isBefore(LocalTime.parse("23:01"))) {
                    // Check if the reservation duration is at least 2 hours
                    if (Duration.between(beginTime, endTime).toHours() >= 2) {
                        // Check if the table is available during the specified time range
                        if (isTableAvailable(tableNumber, beginField.getText(), endField.getText())) {
                            makeReservation(nameField.getText(), emailField.getText(), phoneField.getText(), beginField.getText(), endField.getText(), guestsField.getText(), tableNumber);
                            animateReservation((JButton) e.getSource());
                            guestFrame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(guestFrame, "Table " + tableNumber + " is already reserved during the specified time range.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(guestFrame, "Reservation should be at least 2 hours.");
                    }
                } else {
                    JOptionPane.showMessageDialog(guestFrame, "Reservation should be between 13:00 and 23:00.");
                }
            } else {
                JOptionPane.showMessageDialog(guestFrame, "Please fill in all fields correctly.");
            }
        });

        guestFrame.add(panel);
        animateFrameOpen(guestFrame);
    }

    private boolean validateInput(String name, String email, String phone, String beginTime, String endTime, String guests) {
        return !name.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !beginTime.isEmpty() && !endTime.isEmpty() && !guests.isEmpty();
    }

    private boolean isTableAvailable(int tableNumber, String beginTime, String endTime) {
        try {
            // Convert the provided time strings to LocalDateTime objects
            LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.parse(beginTime));
            LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.parse(endTime));

            // Query to check for overlapping reservations
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT * FROM guests WHERE table_number = ? AND " +
                            "((begin >= ? AND begin < ?) OR (end > ? AND end <= ?) OR " +
                            "(begin <= ? AND end >= ?))");
            pstmt.setInt(1, tableNumber);
            pstmt.setTimestamp(2, Timestamp.valueOf(begin));
            pstmt.setTimestamp(3, Timestamp.valueOf(end));
            pstmt.setTimestamp(4, Timestamp.valueOf(begin));
            pstmt.setTimestamp(5, Timestamp.valueOf(end));
            pstmt.setTimestamp(6, Timestamp.valueOf(begin));
            pstmt.setTimestamp(7, Timestamp.valueOf(end));

            ResultSet rs = pstmt.executeQuery();

            // If there are no overlapping reservations, the table is available
            return !rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false in case of any error
        }
    }

    private void makeReservation(String name, String email, String phone, String beginTime, String endTime, String guests, int tableNumber) {
        LocalTime begin = LocalTime.parse(beginTime);
        LocalTime end = LocalTime.parse(endTime);

        LocalDateTime beginLocalDateTime = LocalDateTime.of(LocalDate.now(), begin);
        LocalDateTime endLocalDateTime = LocalDateTime.of(LocalDate.now(), end);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            // Insert into guests table
            String guestQuery = "INSERT INTO guests (name, email, phone, begin, end, guests, table_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtGuest = connection.prepareStatement(guestQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmtGuest.setString(1, name);
                pstmtGuest.setString(2, email);
                pstmtGuest.setString(3, phone);
                pstmtGuest.setTimestamp(4, Timestamp.valueOf(beginLocalDateTime));
                pstmtGuest.setTimestamp(5, Timestamp.valueOf(endLocalDateTime));
                pstmtGuest.setInt(6, Integer.parseInt(guests));
                pstmtGuest.setInt(7, tableNumber);

                int affectedRows = pstmtGuest.executeUpdate();

                if (affectedRows > 0) {
                    // Retrieve the generated guest_id
                    try (ResultSet generatedKeys = pstmtGuest.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int guestId = generatedKeys.getInt(1);

                            // Insert into tables table
                            String updateQuery = "INSERT INTO tables (table_number, guest_name, begin_time, end_time, number_of_guests, guest_id, reservation_count) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, 1) " +
                                    "ON DUPLICATE KEY UPDATE reservation_count = reservation_count + 1";
                            try (PreparedStatement pstmtTable = connection.prepareStatement(updateQuery)) {
                                pstmtTable.setInt(1, tableNumber);
                                pstmtTable.setString(2, name); // Assuming costumer_name is derived from guest name
                                pstmtTable.setTimestamp(3, Timestamp.valueOf(beginLocalDateTime));
                                pstmtTable.setTimestamp(4, Timestamp.valueOf(endLocalDateTime));
                                pstmtTable.setInt(5, Integer.parseInt(guests));
                                pstmtTable.setInt(6, guestId);

                                pstmtTable.executeUpdate();
                            }

                            JOptionPane.showMessageDialog(null, "Reservation for Table " + tableNumber + " made successfully!");
                            refreshSeatingPlan(); // Refresh the seating plan after making a reservation
                        } else {
                            throw new SQLException("Creating guest failed, no ID obtained.");
                        }
                    }
                } else {
                    throw new SQLException("Creating guest failed, no rows affected.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error making reservation: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void updateTableReservationCount(int tableNumber) {
        tableReservationCount.put(tableNumber, tableReservationCount.getOrDefault(tableNumber, 0) + 1);
        if (tableReservationCount.get(tableNumber) >= 5) {
            // Remove the table from the layout if it's reserved 5 times
            removeTableFromLayout(tableNumber);
        }
    }

    private void removeTableFromLayout(int tableNumber) {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel tablePanel = (JPanel) component;
                JLabel tableNumberLabel = (JLabel) tablePanel.getComponent(1);
                int currentTableNumber = Integer.parseInt(tableNumberLabel.getText());
                if (currentTableNumber == tableNumber) {
                    remove(tablePanel);
                    revalidate();
                    repaint();
                    break;
                }
            }
        }
    }

    private void showManagerInterface() {
        JFrame managerLoginFrame = new JFrame("Manager Login");
        managerLoginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        managerLoginFrame.setSize(350, 150);
        managerLoginFrame.setLocationRelativeTo(null);
        managerLoginFrame.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 2));

        JTextField managerNameField = new JTextField();
        JTextField managerPasswordField = new JPasswordField();
        JButton loginButton = createButton("Login");

        loginButton.addActionListener(e -> {
            String managerName = managerNameField.getText();
            String managerPassword = managerPasswordField.getText();

            if (validateManager(managerName, managerPassword)) {
                JOptionPane.showMessageDialog(managerLoginFrame, "Manager login successful");
                managerLoginFrame.dispose();
                showManagerDashboard();
            } else {
                JOptionPane.showMessageDialog(managerLoginFrame, "Invalid manager name or password");
            }
        });

        panel.add(new JLabel("Manager Name:"));
        panel.add(managerNameField);
        panel.add(new JLabel("Password:"));
        panel.add(managerPasswordField);
        panel.add(loginButton);

        managerLoginFrame.add(panel);
        animateFrameOpen(managerLoginFrame);
    }

    private void showManagerDashboard() {
        JFrame managerFrame = new JFrame("Manager Interface");
        managerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        managerFrame.setSize(700, 500);
        managerFrame.setLocationRelativeTo(null);
        managerFrame.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());

        // Initialize reservationTable
        reservationTable = new JTable();

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM guests");

            int totalReservations = 0;
            int totalGuests = 0;

            while (rs.next()) {
                totalReservations++;
                totalGuests += rs.getInt("guests");
            }

            JLabel totalReservationsLabel = new JLabel("Total Reservations: " + totalReservations);
            JLabel totalGuestsLabel = new JLabel("Total Guests: " + totalGuests);

            JPanel statsPanel = new JPanel(new GridLayout(1, 2));
            statsPanel.add(totalReservationsLabel);
            statsPanel.add(totalGuestsLabel);

            panel.add(statsPanel, BorderLayout.NORTH);

            // Update the JTable model with the fetched data
            reservationTable.setModel(new ResultSetTableModel(rs));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(managerFrame, "Error fetching reservations: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JPanel buttonPanel = new JPanel();
        JButton editButton = createButton("Edit");
        JButton deleteButton = createButton("Delete");
        JButton refreshButton = createButton("Refresh");

        editButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow >= 0) {
                int guestId = (int) reservationTable.getValueAt(selectedRow, 0);
                showEditReservationForm(guestId);
            } else {
                JOptionPane.showMessageDialog(managerFrame, "Please select a reservation to edit.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow >= 0) {
                int guestId = (int) reservationTable.getValueAt(selectedRow, 0);
                int tableNumber = (int) reservationTable.getValueAt(selectedRow, 6); // Assuming table number is in the 7th column
                deleteReservation(guestId, tableNumber);

                // After deletion, update total reservations and guests counts
                refreshReservationStatistics(managerFrame);
            } else {
                JOptionPane.showMessageDialog(managerFrame, "Please select a reservation to delete.");
            }
        });

        refreshButton.addActionListener(e -> {
            refreshSeatingPlan();
            refreshReservationStatistics(managerFrame);
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        managerFrame.add(panel);
        animateFrameOpen(managerFrame);
    }

    private void refreshReservationStatistics(JFrame managerFrame) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS totalReservations, SUM(guests) AS totalGuests FROM guests");

            if (rs.next()) {
                int totalReservations = rs.getInt("totalReservations");
                int totalGuests = rs.getInt("totalGuests");

                // Update labels with new values
                Component[] components = ((JPanel) managerFrame.getContentPane().getComponent(0)).getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {
                        JPanel statsPanel = (JPanel) component;
                        JLabel totalReservationsLabel = (JLabel) statsPanel.getComponent(0); // Assuming total reservations label is first
                        JLabel totalGuestsLabel = (JLabel) statsPanel.getComponent(1); // Assuming total guests label is second
                        totalReservationsLabel.setText("Total Reservations: " + totalReservations);
                        totalGuestsLabel.setText("Total Guests: " + totalGuests);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(managerFrame, "Error refreshing statistics: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void showEditReservationForm(int guestId) {
        JFrame editFrame = new JFrame("Edit Reservation");
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editFrame.setSize(300, 250);
        editFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(8, 2));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField beginField = new JTextField();
        JTextField endField = new JTextField();
        JTextField guestsField = new JTextField();
        JTextField tableField = new JTextField();
        JButton saveButton = createButton("Save");

        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM guests WHERE id = ?");
            pstmt.setInt(1, guestId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                beginField.setText(rs.getTimestamp("begin").toLocalDateTime().toLocalTime().toString());
                endField.setText(rs.getTimestamp("end").toLocalDateTime().toLocalTime().toString());
                guestsField.setText(String.valueOf(rs.getInt("guests")));
                tableField.setText(String.valueOf(rs.getInt("table_number")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(editFrame, "Error fetching reservation details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Reservation Begin (HH:mm):"));
        panel.add(beginField);
        panel.add(new JLabel("Reservation End (HH:mm):"));
        panel.add(endField);
        panel.add(new JLabel("Number of Guests:"));
        panel.add(guestsField);
        panel.add(new JLabel("Table Number:"));
        panel.add(tableField);
        panel.add(new JLabel(""));
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String beginTimeString = beginField.getText();
            String endTimeString = endField.getText();
            int guests = Integer.parseInt(guestsField.getText());
            int table = Integer.parseInt(tableField.getText());

            LocalTime beginTime = LocalTime.parse(beginTimeString);
            LocalTime endTime = LocalTime.parse(endTimeString);

            LocalDateTime beginLocalDateTime = LocalDateTime.of(LocalDate.now(), beginTime);
            LocalDateTime endLocalDateTime = LocalDateTime.of(LocalDate.now(), endTime);

            Timestamp begin = Timestamp.valueOf(beginLocalDateTime);
            Timestamp end = Timestamp.valueOf(endLocalDateTime);

            try {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE guests SET name = ?, email = ?, phone = ?,begin = ?, end = ?,guests = ?, table_number = ? WHERE id = ?");
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setTimestamp(4, begin);
                pstmt.setTimestamp(5, end);
                pstmt.setInt(6, guests);
                pstmt.setInt(7, table);
                pstmt.setInt(8, guestId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(editFrame, "Reservation updated successfully!");
                editFrame.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editFrame, "Error updating reservation: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editFrame.add(panel);
        animateFrameOpen(editFrame);
    }
    private void deleteReservation(int guestId, int tableNumber) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "admin")) {
            connection.setAutoCommit(false);  // Start transaction

            try {
                // First, delete from the guests table
                String deleteGuestQuery = "DELETE FROM guests WHERE id = ?";
                try (PreparedStatement pstmt1 = connection.prepareStatement(deleteGuestQuery)) {
                    pstmt1.setInt(1, guestId);
                    int deletedRowsGuests = pstmt1.executeUpdate();
                }

                // Decrement the reservation count for the table in the tables table
                decrementTableReservationCount(connection, tableNumber);

                connection.commit();  // Commit transaction

                JOptionPane.showMessageDialog(null, "Reservation deleted successfully!");
                refreshSeatingPlan(); // Refresh the seating plan after deleting a reservation
            } catch (SQLException e) {
                connection.rollback();  // Rollback transaction in case of error
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error deleting reservation: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void decrementTableReservationCount(Connection connection, int tableNumber) throws SQLException {
        String selectQuery = "SELECT reservation_count FROM tables WHERE table_number = ?";
        String decrementQuery = "UPDATE tables SET reservation_count = GREATEST(reservation_count - 1, 0) WHERE table_number = ?";

        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, tableNumber);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int currentCount = rs.getInt("reservation_count");

                    if (currentCount > 0) {
                        try (PreparedStatement decrementStmt = connection.prepareStatement(decrementQuery)) {
                            decrementStmt.setInt(1, tableNumber);
                            int rowsAffected = decrementStmt.executeUpdate();

                            if (rowsAffected == 0) {
                                throw new SQLException("Failed to decrement reservation count for table number " + tableNumber);
                            } else {
                                System.out.println("Reservation count decremented successfully for table number " + tableNumber);
                            }
                        }
                    } else {
                        throw new SQLException("Reservation count for table number " + tableNumber + " is already zero.");
                    }
                } else {
                    throw new SQLException("Table number " + tableNumber + " not found or no reservation exists.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in decrementTableReservationCount: " + e.getMessage());
            e.printStackTrace();
            throw e; // rethrow the exception to handle it at the higher level
        }
    }



    private void refreshSeatingPlan() {
        // Remove all components from the seating panel and recreate the seating plan
        remove(1); // Index 1 corresponds to the seating panel
        createSeatingPlan();
        revalidate();
        repaint();
        refreshReservationTable();
    }

    private void refreshReservationTable() {
        // Retrieve the data from the database and update the JTable
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM guests"); // Ensure the correct table is queried
            reservationTable.setModel(new ResultSetTableModel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching reservations: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void animateFrameOpen(JFrame frame) {
        if (!frame.isUndecorated()) { // Check if the frame is decorated
            frame.setVisible(true); // If decorated, just make it visible without opacity animation
            return;
        }

        // Fade in animation for undecorated frames
        frame.setOpacity(0.0f);
        frame.setVisible(true);

        Timer fadeInTimer = new Timer(20, new ActionListener() {
            float opacity = 0.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                frame.setOpacity(Math.min(opacity, 1.0f));
                if (opacity >= 1.0f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        fadeInTimer.start();
    }

    private void animateReservation(JButton button) {
        // Blink animation for reservation button
        Timer blinkTimer = new Timer(200, new ActionListener() {
            boolean isVisible = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                isVisible = !isVisible;
                button.setVisible(isVisible);
            }
        });
        blinkTimer.setRepeats(false);
        blinkTimer.start();
    }

}