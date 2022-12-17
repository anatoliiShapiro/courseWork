import entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class RegistrationForm extends JDialog {
    private JTextField tfName;
    private JTextField pfPassword;
    private JTextField tfEmail;
    private JButton applyButton;
    private JButton cancelButton;
    private JPanel registerPanel;
    public User user;

    private final String NAME_REGEX = "[A-Za-z]+['-]?([A-Za-z]['-])?[A-Za-z]+ ?";
    private final String EMAIL_REGEX = "^[\\w-\\.]+@[\\w]+\\.[\\w]+";
    public RegistrationForm(JFrame parent) {
        super(parent);
        setTitle("Create a new account");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(500, 300));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                registerUser();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });

        setVisible(true);
    }
    private void registerUser() {
        String name = tfName.getText();
        String email = tfEmail.getText();
        String password = String.valueOf(pfPassword.getText());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all fields", "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isValidInput(name, email)) {
            user = saveUserToDataBase(name, email, password);
            if (user != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "entities.User exists!", "Try again",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid name or e-mail!", "Try again",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidInput(String name, String email) {
        boolean isCorrect = false;

        if (name.matches(NAME_REGEX) && name.length() > 1) {
            if (email.matches(EMAIL_REGEX) && email.length() > 1) {
                isCorrect = true;
            }
        }
        return isCorrect;
    }
    private User saveUserToDataBase(String name, String email, String password) {
        User user = null;
        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            Statement statement = connection.createStatement();
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, String.valueOf(password.hashCode()));

            int addedRows = preparedStatement.executeUpdate();
            if (addedRows > 0) {
                user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPassword(password);
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}
