import entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardForm extends JFrame {
    private JPanel dashboardPanel;
    private JButton loginButton;
    private JButton registerButton;

    public DashboardForm() {
        setTitle("Dashboard");
        setContentPane(dashboardPanel);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width - 150, dim.height / 3 - this.getSize().height / 3);
        setSize(300, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        connectToDatabase();

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
                LoginForm loginForm = new LoginForm(DashboardForm.this);
                User user = loginForm.user;
            }

        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
                RegistrationForm registrationForm = new RegistrationForm(DashboardForm.this);
                User user = registrationForm.user;

                if (user != null) {
                    JOptionPane.showMessageDialog(DashboardForm.this,
                            "New user: " + user.getName(),
                            "Successful Registration",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                new DashboardForm();
            }
        });

        setVisible(true);
    }

    private boolean connectToDatabase() {
        boolean hasRegisteredUsers = false;

        final String MYSQL_SERVER_URL = "jdbc:mysql://127.0.0.1/";
        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";

        try {
            Connection connection = DriverManager.getConnection(MYSQL_SERVER_URL, USERNAME, PASSWORD);

            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS coursework");
            statement.close();
            connection.close();


            connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = connection.createStatement();
            String sqlUser = "CREATE TABLE IF NOT EXISTS users ("
                    + "id_user INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "email VARCHAR(50) NOT NULL UNIQUE,"
                    + "password VARCHAR(50) NOT NULL"
                    + ");";
            statement.executeUpdate(sqlUser);

            String sqlFile = "CREATE TABLE IF NOT EXISTS file ("
                    + "id_file INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "encrypted BOOLEAN,"
                    + "fk_user INT NOT NULL,"
                    + "CONSTRAINT fk_user "
                    + "FOREIGN KEY (fk_user) "
                    + "REFERENCES users (id_user) "
                    + "ON DELETE CASCADE "
                    + "ON UPDATE CASCADE "
                    + ")";
            statement.executeUpdate(sqlFile);

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegisteredUsers = true;
                }
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegisteredUsers;
    }

    public static void start() {
        new DashboardForm();
    }
}
