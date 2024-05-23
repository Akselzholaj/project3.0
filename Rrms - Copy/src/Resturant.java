import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Resturant extends JPanel {

    private Connection connection;

    public Resturant(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());

        JButton guestButton = new JButton("New Reservation");
        JButton managerButton = new JButton("Manager Interface");

        guestButton.addActionListener(e -> showGuestForm(0)); // Placeholder table number
        managerButton.addActionListener(e -> showManagerInterface());

        JPanel panel = new JPanel();
        panel.add(guestButton);
        panel.add(managerButton);

        add(panel, BorderLayout.NORTH);

        createSeatingPlan();
    }

    private void createSeatingPlan() {
        JPanel seatingPanel = new JPanel(new GridLayout(3, 6, 40, 30));
        seatingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 1; i <= 18; i++) {
            try {
                ImageIcon tableImage = new ImageIcon(getClass().getResource("table.png"));

                JButton tableButton = new JButton("Table " + i, tableImage);
                tableButton.setPreferredSize(new Dimension(100, 100));
                tableButton.setFocusPainted(false);
                tableButton.setContentAreaFilled(false);

                int tableNumber = i;
                tableButton.addActionListener(e -> {
                    if (askForReservation(tableNumber)) {
                        showGuestForm(tableNumber);
                    } else {
                        JOptionPane.showMessageDialog(null, "Reservation canceled for Table " + tableNumber);
                    }
                });

                seatingPanel.add(tableButton);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating table: " + ex.getMessage(), "Table Creation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        add(seatingPanel, BorderLayout.CENTER);
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

    private void showLoginDialog() {
        JFrame loginFrame = new JFrame("Reserve a table");
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginFrame.setSize(300, 150);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(2, 1));

        JButton managerLoginButton = new JButton("Manager Login");
        managerLoginButton.addActionListener(e -> {
            String managerName = JOptionPane.showInputDialog(loginFrame, "Enter Manager Name:");
            if (managerName != null) {
                String managerPassword = JOptionPane.showInputDialog(loginFrame, "Enter Manager Password:");
                if (managerPassword != null) {
                    if (validateManager(managerName, managerPassword)) {
                        JOptionPane.showMessageDialog(loginFrame, "Manager login successful");
                        loginFrame.dispose();
                        showManagerInterface();
                    } else {
                        JOptionPane.showMessageDialog(loginFrame, "Invalid name or password");
                    }
                }
            }
        });

        JButton guestLoginButton = new JButton("Make a reservation");
        guestLoginButton.addActionListener(e -> JOptionPane.showMessageDialog(loginFrame, "Please select a table to reserve."));

        panel.add(managerLoginButton);
        panel.add(guestLoginButton);

        loginFrame.add(panel);
        loginFrame.setVisible(true);
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

        JPanel panel = new JPanel(new GridLayout(8, 2));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField beginField = new JTextField();
        JTextField endField = new JTextField();
        JTextField guestsField = new JTextField();
        JTextField tableField = new JTextField(String.valueOf(tableNumber));
        JButton reserveButton = new JButton("Reserve");

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
        panel.add(reserveButton);

        reserveButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String beginTimeString = beginField.getText();
            String endTimeString = endField.getText();
            int guests = Integer.parseInt(guestsField.getText());

            LocalTime beginTime = LocalTime.parse(beginTimeString);
            LocalTime endTime = LocalTime.parse(endTimeString);

            LocalDateTime beginLocalDateTime = LocalDateTime.of(LocalDate.now(), beginTime);
            LocalDateTime endLocalDateTime = LocalDateTime.of(LocalDate.now(), endTime);

            Timestamp begin = Timestamp.valueOf(beginLocalDateTime);
            Timestamp end = Timestamp.valueOf(endLocalDateTime);

            Guest guest = new Guest(0, name, email, phone, begin.toString(), end.toString(), guests, tableNumber);
            guest.insertGuest();

            Table tableReservation = new Table(tableNumber, name, begin.toString(), end.toString(), guests);
            tableReservation.insertTableReservation();

            JOptionPane.showMessageDialog(guestFrame, "Reservation for Table " + tableNumber + " made successfully!");
            guestFrame.dispose();
        });

        guestFrame.add(panel);
        guestFrame.setVisible(true);
    }

    private void showManagerInterface() {
        JFrame managerLoginFrame = new JFrame("Manager Login");
        managerLoginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        managerLoginFrame.setSize(300, 150);
        managerLoginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1));

        JTextField managerNameField = new JTextField();
        JTextField managerPasswordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

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
        managerLoginFrame.setVisible(true);
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
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

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
                refreshReservationTable(); // Refresh the reservation table after deletion
            } else {
                JOptionPane.showMessageDialog(managerFrame, "Please select a reservation to delete.");
            }
        });

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        managerFrame.add(panel);
        managerFrame.setVisible(true);
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
        JButton saveButton = new JButton("Save");

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
        editFrame.setVisible(true);
    }

    private void deleteReservation(int guestId, int tableNumber) {
        try {
            // First, delete from the guests table
            PreparedStatement pstmt1 = connection.prepareStatement("DELETE FROM guests WHERE id = ?");
            pstmt1.setInt(1, guestId);
            pstmt1.executeUpdate();

            // Next, delete from the tables table
            PreparedStatement pstmt2 = connection.prepareStatement("DELETE FROM tables WHERE table_number = ? AND costumer_name = ?");
            pstmt2.setInt(1, tableNumber);
            pstmt2.setInt(2, guestId);
            pstmt2.executeUpdate();

            JOptionPane.showMessageDialog(null, "Reservation deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting reservation: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void refreshReservationTable() {
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM guests");
            JTable reservationTable = new JTable(new ResultSetTableModel(rs));
            JScrollPane scrollPane = new JScrollPane(reservationTable);
            JOptionPane.showMessageDialog(null, scrollPane, "Reservations", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching reservations: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
