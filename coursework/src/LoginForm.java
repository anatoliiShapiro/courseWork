import entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JDialog {
    private JPanel loginPanel;
    private JTextField tfEmail;
    private JPasswordField pfPassword;
    private JButton OKButton;
    private JButton cancelButton;
    private final String EMAIL_REGEX = "^[\\w-\\.]+@[\\w]+\\.[\\w]+";

    public User user;

    public LoginForm(JFrame dashboard) {
        super(dashboard);
        setTitle("Login");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(500, 300));
        setModal(true);
        setLocationRelativeTo(dashboard);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String email = tfEmail.getText();
                String password = String.valueOf(pfPassword.getPassword());

                if (isValidInput(email)) {
                    user = getAuthenticatedUser(email, password);

                    if (user != null) {
                        dispose();
                        TextForm textForm = new TextForm(user,dashboard);
                    } else {
                        JOptionPane.showMessageDialog(LoginForm.this, "Email or password incorrect",
                                "Try again", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(LoginForm.this, "Email or password incorrect",
                            "Try again", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
                dashboard.setVisible(true);
            }
        });

        setVisible(true);
    }

    private User getAuthenticatedUser(String email, String password) {
        User user = null;

        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            Statement statement = connection.createStatement();
            String sql = "SELECT * FROM users WHERE email=? AND password=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, String.valueOf(password.hashCode()));

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id_user"));
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    private boolean isValidInput(String email) {
        boolean isCorrect = false;

            if (email.matches(EMAIL_REGEX) && email.length() > 1) {
                isCorrect = true;
            }
        return isCorrect;
    }
}
