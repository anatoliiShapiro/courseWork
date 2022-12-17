import entities.FileUser;
import entities.User;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.Base64;

public class TextForm extends JFrame {
    private JTextArea tfTextArea;
    private JButton createButton;
    private JButton encryptButton;
    private JButton saveButton;
    private JPanel FilePanel;
    private JButton exitButton;
    private JButton decodeButton;
    FileUser fileUser = new FileUser();
    private final String path = "E:\\coursework\\src\\userFile\\";
    private final String pathKey = "E:\\coursework\\src\\codeKeyFile\\";

    private static Cipher cipher;
    private static boolean encrypted = false;
    public TextForm(User user, JFrame dashboard) {
        setTitle("User File");
        setContentPane(FilePanel);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width - 250, dim.height / 3 - this.getSize().height / 3);
        setSize(500, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        tfTextArea.setLineWrap(true);
        tfTextArea.setText(readFile(user));


        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createFile(user);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
                dashboard.setVisible(true);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                writeToFile(user);
                JOptionPane.showMessageDialog(TextForm.this, "File saved! User: " + user.getName(),
                        "Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!checkEncrypted(user)) {
                    String clearText = readFile(user);
                    String encodeText = encode(clearText, fileUser.getSecretKey());
                    tfTextArea.setText(encodeText);
                    writeToFile(user);
                    isEncrypted(user);
                } else {
                    JOptionPane.showMessageDialog(TextForm.this, "The file is already encrypted! Decipher it!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        decodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (checkEncrypted(user)) {
                    String encryptedText = readFile(user);
                    String decryptedText = decode(encryptedText, fileUser.getSecretKey());
                    tfTextArea.setText(decryptedText);
                    writeToFile(user);
                    isDecrypted(user);
                } else {
                    JOptionPane.showMessageDialog(TextForm.this, "The file is already decrypted!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        byte[] keyArrayByte = readKeyFile(user);
        fileUser.getKey(keyArrayByte);

        setVisible(true);
    }

    private void createFile(User user) {
        String fileName = user.getName() + ".txt";

        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";

        try {
            File myObj = new File(path + fileName);
            if (myObj.createNewFile()) {
                JOptionPane.showMessageDialog(TextForm.this, "File created: " + myObj.getName(),
                        "Successful", JOptionPane.INFORMATION_MESSAGE);
                File fileKey = new File(pathKey + fileName);
                if (fileKey.createNewFile()) {
                    try {
                        FileWriter fileKeyy = new FileWriter(pathKey + fileName);
                        byte[] keyArrayByte = fileUser.createKey();
                        fileUser.getKey(keyArrayByte);
                        for (int i =0; i < keyArrayByte.length; i++) {
                            fileKeyy.write(keyArrayByte[i] + "\n");
                        }
                        fileKeyy.flush();
                        fileKeyy.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    byte[] keyArrayByte = readKeyFile(user);
                    fileUser.getKey(keyArrayByte);
                }



                Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

                String sql = "INSERT INTO file(name, encrypted, fk_user) VALUES(?,?,?)";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, fileName);
                preparedStatement.setBoolean(2, false);
                preparedStatement.setInt(3, user.getId());
                preparedStatement.executeUpdate();

                connection.close();
            } else {
                JOptionPane.showMessageDialog(TextForm.this, "File already exists.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(User user) {
        String fileName = user.getName() + ".txt";

        try {
            FileWriter userFile = new FileWriter(path + fileName);
            userFile.write(tfTextArea.getText());
            userFile.flush();
            userFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(User user) {
        String fileName = user.getName() + ".txt";
        String allLines = "";

        File file = new File(path + fileName);

        if (file.exists() && !file.isDirectory()) {
            try {
                FileReader r = new FileReader(path + fileName);
                int i;
                while ((i = r.read()) != -1) {
                    allLines += (char) i;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return allLines;
    }

    private byte[] readKeyFile(User user) {
        String fileName = user.getName() + ".txt";
        byte[] array = new byte[16];

        File file = new File(pathKey + fileName);

        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(pathKey + fileName));
                int i = 0;
                String line = reader.readLine();;
                int intLine = 0;
                char charInt;

                while (line != null) {
                    intLine = Integer.parseInt(line);
                    charInt = (char) intLine;

                    array[i] = (byte) charInt;
                    i++;
                    line = reader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return array;
    }

    private String encode(String strClearText, SecretKey secretKey) {
        String encryptedText = "";

        try {
            cipher = Cipher.getInstance("AES");
            byte[] strClearTextByte = strClearText.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedByte = cipher.doFinal(strClearTextByte);
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(encryptedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedText;
    }

    private String decode(String strEncrypted, SecretKey secretKey) {
        String decryptedText = "";
        try {
            cipher = Cipher.getInstance("AES");
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] encryptedTextByte = decoder.decode(strEncrypted);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
            decryptedText = new String(decryptedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptedText;
    }

    private boolean checkEncrypted(User user) {
        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "SELECT encrypted FROM file WHERE fk_user=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                encrypted = resultSet.getBoolean("encrypted");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return encrypted;
    }

    private boolean isEncrypted(User user) {
        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "UPDATE file SET encrypted=? WHERE fk_user=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, user.getId());
            preparedStatement.executeUpdate();

            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return encrypted;
    }

    private boolean isDecrypted(User user) {
        final String DB_URL = "jdbc:mysql://127.0.0.1/coursework";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "UPDATE file SET encrypted=? WHERE fk_user=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBoolean(1, false);
            preparedStatement.setInt(2, user.getId());
            preparedStatement.executeUpdate();

            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return encrypted;
    }

}
