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





public class Resturant extends JPanel {

    private Connection connection;
    private Map<Integer, Integer> tableReservationCount;

    // Dark theme colors
    private Color darkBackground = new Color(34, 34, 34);
    private Color darkForeground = Color.WHITE;
    private Color darkButtonBackground = new Color(59, 89, 152);
    private Color darkButtonForeground = Color.WHITE;

    public Resturant(Connection connection) {
        this.connection = connection;
        this.tableReservationCount = new HashMap<>();
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
        JPanel seatingPanel = new JPanel(new GridLayout(3, 6, 40, 30));
        seatingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        seatingPanel.setBackground(darkBackground);

        for (int i = 1; i <= 18; i++) {
            try {
                ImageIcon tableImage = new ImageIcon(getClass().getResource("table.png"));

                JButton tableButton = new JButton(tableImage);
                tableButton.setPreferredSize(new Dimension(100, 100));
                tableButton.setFocusPainted(false);
                tableButton.setContentAreaFilled(false);
                tableButton.setForeground(darkForeground);
                tableButton.setHorizontalTextPosition(JButton.CENTER);
                tableButton.setVerticalTextPosition(JButton.CENTER);
                tableButton.setText(Integer.toString(i)); // Set the table number as text

                JLabel tableNumberLabel = new JLabel(Integer.toString(i));
                tableNumberLabel.setForeground(darkForeground);
                tableNumberLabel.setHorizontalAlignment(JLabel.CENTER);

                JPanel tablePanel = new JPanel(new BorderLayout());
                tablePanel.setOpaque(false);
                tablePanel.add(tableButton, BorderLayout.CENTER);
                tablePanel.add(tableNumberLabel, BorderLayout.NORTH);

                addTableButtonListener(tableButton, i);

                seatingPanel.add(tablePanel);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating table: " + ex.getMessage(), "Table Creation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        add(seatingPanel, BorderLayout.CENTER);
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
                tableButton.setPreferredSize(new Dimension(110, 110));
                tableButton.revalidate();
                tableButton.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Restore original button size when mouse exits
                tableButton.setPreferredSize(new Dimension(100, 100));
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
        guestFrame.setSize(300, 250);
        guestFrame.setLocationRelativeTo(null);
        guestFrame.setBackground(darkBackground);

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
        panel.add(new JLabel("Begin(HH:mm):"));
        panel.add(beginField);
        panel.add(new JLabel("End(HH:mm):"));
        panel.add(endField);
        panel.add(new JLabel("Number of Guests:"));
        panel.add(guestsField);
        panel.add(new JLabel("Table Number:"));
        panel.add(tableField);
        panel.add(new JLabel(""));
        panel.add(reserveButton);

        reserveButton.addActionListener(e -> {
            if (validateInput(nameField.getText(), emailField.getText(), phoneField.getText(), beginField.getText(), endField.getText(), guestsField.getText())) {
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

        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO guests (name, email, phone, begin, end, guests, table_number) VALUES (?, ?, ?, ?, ?, ?, ?)");
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setTimestamp(4, Timestamp.valueOf(beginLocalDateTime));
            pstmt.setTimestamp(5, Timestamp.valueOf(endLocalDateTime));
            pstmt.setInt(6, Integer.parseInt(guests));
            pstmt.setInt(7, tableNumber);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Reservation for Table " + tableNumber + " made successfully!");
            updateTableReservationCount(tableNumber); // Update the reservation count for the table
            refreshSeatingPlan(); // Refresh the seating plan after making a reservation
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
        managerLoginFrame.setSize(300, 150);
        managerLoginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1));

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
        managerFrame.setSize(600, 400);
        managerFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JTable reservationTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM guests");
            reservationTable.setModel(new ResultSetTableModel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(managerFrame, "Error fetching reservations: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JPanel buttonPanel = new JPanel();
        JButton editButton = createButton("Edit");
        JButton deleteButton = createButton("Delete");

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
            } else {
                JOptionPane.showMessageDialog(managerFrame, "Please select a reservation to delete.");
            }
        });

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        managerFrame.add(panel);
        animateFrameOpen(managerFrame);
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
        try {
            // First, delete from the guests table
            PreparedStatement pstmt1 = connection.prepareStatement("DELETE FROM guests WHERE id = ?");
            pstmt1.setInt(1, guestId);
            pstmt1.executeUpdate();

            JOptionPane.showMessageDialog(null, "Reservation deleted successfully!");
            updateTableReservationCount(tableNumber); // Update the reservation count for the table
            refreshSeatingPlan(); // Refresh the seating plan after deleting a reservation
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting reservation: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSeatingPlan() {
        // Remove all components from the seating panel and recreate the seating plan
        remove(1); // Index 1 corresponds to the seating panel
        createSeatingPlan();
        revalidate();
        repaint();
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
