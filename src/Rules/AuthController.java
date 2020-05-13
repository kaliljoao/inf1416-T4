package Rules;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AuthController {

    private static AuthController auth = null;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();


    private AuthController() {
    }

    public static AuthController getInstance() {
        if (auth == null) {
            synchronized (AuthController.class) {
                auth = new AuthController();
            }
        }
        return auth;
    }

    public static boolean findUserByLogin(String email) {
        try {
            ResultSet rs = null;
            DbSingletonController.createConnection();
            DbSingletonController.createStatement();
            rs = DbSingletonController.executeQuery(String.format("select * from Usuario where email = '%s'", email));
            if (rs != null && rs.next()) {
                String login = rs.getString(2);
                if (email.equalsIgnoreCase(login)) {
                    DbSingletonController.closeConnection();
                    return true;
                }
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validatePassword(String login, ArrayList<Object> digits) throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery(String.format("select hashedPassword ,password_key from Usuario where email='%s'", login));
        if (rs != null && rs.next()) {
            String dbhashedPassword = rs.getString(1);
            int dbSalt = rs.getInt(2);
            int length = digits.size();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        for (int l = 0; l < 2; l++) {
                            for (int m = 0; m < 2; m++) {
                                for (int n = 0; n < 2; n++) {  // 6 digitos
                                    String password;
                                    String hashedPassword;
                                    if (length > 6) {
                                        if (length == 7) {
                                            for (int o = 0; o < 2; o++) {
                                                password = ((String[]) digits.get(0))[i] + ((String[]) digits.get(1))[j] + ((String[]) digits.get(2))[k] +
                                                        ((String[]) digits.get(3))[l] + ((String[]) digits.get(4))[m] + ((String[]) digits.get(5))[n] + ((String[]) digits.get(6))[o];
                                                hashedPassword = getPasswordHash(password, dbSalt);
                                                if (hashedPassword.equals(dbhashedPassword)) {
                                                    return true;
                                                }
                                            }
                                        } else {
                                            for (int o = 0; o < 2; o++) {
                                                for (int p = 0; p < 2; p++) {
                                                    password = ((String[]) digits.get(0))[i] + ((String[]) digits.get(1))[j] + ((String[]) digits.get(2))[k] +
                                                            ((String[]) digits.get(3))[l] + ((String[]) digits.get(4))[m] + ((String[]) digits.get(5))[n] + ((String[]) digits.get(6))[o]
                                                            + ((String[]) digits.get(7))[p];
                                                    hashedPassword = getPasswordHash(password, dbSalt);
                                                    if (hashedPassword.equals(dbhashedPassword)) {
                                                        return true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        password = ((String[]) digits.get(0))[i] + ((String[]) digits.get(1))[j] + ((String[]) digits.get(2))[k] +
                                                ((String[]) digits.get(3))[l] + ((String[]) digits.get(4))[m] + ((String[]) digits.get(5))[n];
                                        hashedPassword = getPasswordHash(password, dbSalt);
                                        if (hashedPassword.equals(dbhashedPassword)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    private static String getPasswordHash(String password, Integer salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        password = password + String.format("%09d", salt);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String ret = bytesToHex(md5.digest(password.getBytes("UTF-8")));
            return ret;
        } catch (NoSuchAlgorithmException ex) {
            throw ex;
        } catch (UnsupportedEncodingException ex) {
            throw ex;
        }
    }
}