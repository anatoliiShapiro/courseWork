package entities;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class FileUser {
    private String name;

    private boolean encrypted;
    SecretKey secretKey;


    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void getKey(byte[] array) {
        secretKey = new SecretKeySpec(array,0,array.length, "AES");
    }

    public byte[] createKey() {
        SecretKey sk = null;
        byte[] array = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            sk = keyGenerator.generateKey();
            array = sk.getEncoded();

           /* System.out.println("KEY = " + sk);

           for (int i =0; i < array.length; i++) {
                System.out.print(array[i] + " ");
            }
            System.out.println();


            SecretKey secretKey1 = new SecretKeySpec(array,0,array.length, "AES");
            System.out.println("KEY2 = " + secretKey1);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }
}
