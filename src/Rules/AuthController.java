package Rules;

import javax.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;


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
                String login = rs.getString(3);
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
        rs = DbSingletonController.executeQuery(String.format("select hashedPassword ,salt from Usuario where email='%s'", login));
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

    public static PrivateKey getBased64PrivateKey (String fraseSecreta, File userKeyFile)
            throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
            IOException, BadPaddingException, IllegalBlockSizeException,
            InvalidKeySpecException
    {
        SecureRandom pnrg = SecureRandom.getInstance("SHA1PRNG");
        byte[] fraseSecretaBytes = fraseSecreta.getBytes();
        pnrg.setSeed(fraseSecretaBytes);

        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        keyGen.init(pnrg);
        Key key = keyGen.generateKey();


        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        Path keyPath = Paths.get(userKeyFile.getPath());
        byte[] newPlainText = cipher.doFinal(Files.readAllBytes(keyPath));
        String userKeyBased64 = new String(newPlainText, "UTF8");

        String[] userKeyBased64Array = Arrays.copyOfRange(userKeyBased64.split("\n"),1,userKeyBased64.split("\n").length -1);
        String only64BasedPrivateKey = Arrays.toString(userKeyBased64Array);
        only64BasedPrivateKey = only64BasedPrivateKey.substring(1,only64BasedPrivateKey.length() - 1);
        byte[] encodedKeybased64 = Base64.getMimeDecoder().decode(only64BasedPrivateKey);

        PKCS8EncodedKeySpec encoded = new PKCS8EncodedKeySpec(encodedKeybased64);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(encoded);
    }

    public static void setUserCertificate(String login) throws SQLException, FileNotFoundException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();

        String query = "Update Usuario" +
                       " set certificado = ?" +
                       " where email='jpkalil@keener.io' ";

        PreparedStatement statement = DbSingletonController.setPreparedStatement(query);

        FileReader reader = new FileReader("Keys/user01-x509.crt");
        statement.setCharacterStream(1, reader);
        statement.execute();

    }

    public static PublicKey getUserPublicKeyFromCertificate (String login) throws SQLException, CertificateException {
        ArrayList<String> Based64Certificate = new ArrayList<String>();
        String cert = "";

        boolean is64BasedCertificate = false;
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery(String.format("select certificado from Usuario where email='%s'", login));
        if (rs != null && rs.next()) {
            String certificado = rs.getString(1);
            String[] certificadoArray = certificado.split("\n");
            for(String linha: certificadoArray) {
                if (linha.equals("-----BEGIN CERTIFICATE-----")) {
                     is64BasedCertificate = true;
                     cert+=linha+"\n";
                     continue;
                }
                if(is64BasedCertificate == true){
                    cert+=linha+"\n";
                    continue;
                    //Based64Certificate.add(linha);
                }
            }
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            byte[] b = Based64Certificate.toString().getBytes();
            ByteArrayInputStream bytes = new ByteArrayInputStream(cert.getBytes());
            X509Certificate certificate = (X509Certificate)cf.generateCertificate(bytes);
            return certificate.getPublicKey();
        }
        return null;
    }

}












