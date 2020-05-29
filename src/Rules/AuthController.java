package Rules;

import Models.UserModel;

import javax.crypto.*;
import javax.swing.*;
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
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;


public class AuthController {

    private static AuthController auth = null;
    private static UserModel model;
    private static SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS");

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

    public static void setUserModel(UserModel _model) {
        model = _model;
    }

    public static UserModel getUserModel(){
        return model;
    }

    public static Object[] findUserByLogin(String email) {
        try {
            ResultSet rs = null;
            DbSingletonController.createConnection();
            DbSingletonController.createStatement();
            rs = DbSingletonController.executeQuery(String.format("select LoginNome, isBlocked from Usuario where LoginNome = '%s'", email));
            if (rs != null && rs.next()) {
                String login = rs.getString(1);
                if (email.equalsIgnoreCase(login)) {
                    return new Object[] { true , rs.getInt(2) };
                }
                return new Object[] { false , -1 };
            }
            return new Object[] { false , -1 };
        } catch (Exception e) {
            return new Object[] { false , -1 };
        }
    }

    public static void blockUser (String LoginNome, String dataHora) throws SQLException, ParseException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();

        if(!(boolean)isBlocked(LoginNome)) {
            ResultSet rs = null;
            rs = DbSingletonController.executeQuery(String.format("select * from RegistroAcesso where LoginNome='%s';", LoginNome));
            if (rs != null && rs.next()){
                String query = "UPDATE RegistroAcesso set HoraAcesso = " + String.format("'%s'", dataHora) + " WHERE LoginNome = "+ String.format("'%s';", LoginNome);
                DbSingletonController.executeUpdate(query);
                query = "UPDATE usuario set isBlocked = 1 WHERE LoginNome = "+ String.format("'%s';", LoginNome);
                DbSingletonController.executeUpdate(query);
            }
            else {
                String query = "INSERT INTO RegistroAcesso (LoginNome, HoraAcesso) VALUES " + String.format("('%s','%s');", LoginNome, dataHora);
                DbSingletonController.executeUpdate(query);
                query = "UPDATE usuario set isBlocked = 1 WHERE LoginNome = "+ String.format("'%s';", LoginNome);
                DbSingletonController.executeUpdate(query);
            }
        }
    }

    public static Object isBlocked(String LoginNome) throws SQLException, ParseException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();

        ResultSet rs = null;
        rs = DbSingletonController.executeQuery(String.format("select isBlocked from Usuario where LoginNome='%s';", LoginNome));
        if (rs != null && rs.next()) {
            int isBlocked = rs.getInt(1);
            if(isBlocked == 0) return false;
            else {
                ResultSet new_rs = DbSingletonController.executeQuery(String.format("select HoraAcesso from RegistroAcesso where LoginNome='%s';", LoginNome));
                if (new_rs != null && new_rs.next()){
                    String hora = new_rs.getString(1);
                    Date data = formatter.parse(hora);
                    Date dateMomento = new Date(System.currentTimeMillis());

                    long diferenca = dateMomento.getTime() - data.getTime();

                    if(diferenca > new Long(120000)){
                        unlockUser(LoginNome);
                        return false;
                    }
                    else{
                        return true;
                    }
                }
            }
        }
        return null;
    }

    public static void unlockUser (String LoginNome) throws SQLException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        String query = "UPDATE usuario set isBlocked = 0 WHERE LoginNome = "+ String.format("'%s';", LoginNome);
        DbSingletonController.executeUpdate(query);
    }

    public static void increaseConsultas (String LoginNome, int qtdConsultas) throws SQLException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        String query = "UPDATE usuario set Consultas = "+ String.valueOf(qtdConsultas) +" WHERE LoginNome = "+ String.format("'%s';", LoginNome);
        DbSingletonController.executeUpdate(query);
    }

    public static boolean validatePassword(String login, ArrayList<Object> digits) throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery(String.format("select hashedPassword ,salt from Usuario where LoginNome='%s'", login));
        if (rs != null && rs.next()) {
            String dbhashedPassword = rs.getString(1);
            String dbSalt = rs.getString(2);
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
                                                hashedPassword = getPasswordHash(password, dbSalt, false);
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
                                                    hashedPassword = getPasswordHash(password, dbSalt, false);
                                                    if (hashedPassword.equals(dbhashedPassword)) {
                                                        return true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        password = ((String[]) digits.get(0))[i] + ((String[]) digits.get(1))[j] + ((String[]) digits.get(2))[k] +
                                                ((String[]) digits.get(3))[l] + ((String[]) digits.get(4))[m] + ((String[]) digits.get(5))[n];
                                        hashedPassword = getPasswordHash(password, dbSalt, false);
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

    private static boolean validatePassword (String key) {
        char[] numbers = key.toCharArray();
        for(int i = 0; i < numbers.length; i ++) {
            if((i+1) < numbers.length){
                if(     ((Integer.valueOf(String.valueOf(numbers[i]))+1) == Integer.valueOf(String.valueOf(numbers[i+1])))
                        ||
                        (Integer.valueOf(String.valueOf(numbers[i]))) == Integer.valueOf(String.valueOf(numbers[i+1]))-1 ) {
                    return false;
                }

            }
            else{
                break;
            }
        }
        return true;
    }

    public static String getPasswordHash (String key, String salt, boolean isAltering) throws SQLException {
        if(isAltering) { // Criando usuario ou alterando senha
            if (validatePassword(key)) {
                byte[] calculated_hash;
                String calculated_hash_HEX;
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String combined = key + salt;
                byte[] input = combined.getBytes();
                md.update(input);
                calculated_hash = md.digest();
                calculated_hash_HEX = ByteToString(calculated_hash);
                return calculated_hash_HEX;
            }
            Date date = new Date(System.currentTimeMillis());
            LogController.storeRegistry(6003, formatter.format(date),null, model);
            return "nok";
        }
        else{
            byte[] calculated_hash;
            String calculated_hash_HEX;
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA1");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String combined = key + salt;
            byte[] input = combined.getBytes();
            md.update(input);
            calculated_hash = md.digest();
            calculated_hash_HEX = ByteToString(calculated_hash);
            return calculated_hash_HEX;
        }
    }

    public static String generateSalt() {
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lower = upper.toLowerCase();
        final String digits = "0123456789";
        final String alphanum = upper + lower + digits;

        int count = 10;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*alphanum.length());
            builder.append(alphanum.charAt(character));
        }
        return builder.toString();
    }

    private static String ByteToString(byte[] info) {
        // convert to hexadecimal
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < info.length; i++) {
            String hex = Integer.toHexString(0x0100 + (info[i] & 0x00FF))
                    .substring(1);
            buf.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return buf.toString();
    }


    public static PrivateKey getBased64PrivateKey (String fraseSecreta, File userKeyFile, String Login)
            throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
            IOException, BadPaddingException, IllegalBlockSizeException,
            InvalidKeySpecException, SQLException
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

        byte[] newPlainText = null;
        try {
            newPlainText = cipher.doFinal(Files.readAllBytes(keyPath));
            String userKeyBased64 = new String(newPlainText, "UTF8");
            String[] userKeyBased64Array = Arrays.copyOfRange(userKeyBased64.split("\n"),1,userKeyBased64.split("\n").length -1);
            String only64BasedPrivateKey = Arrays.toString(userKeyBased64Array);
            only64BasedPrivateKey = only64BasedPrivateKey.substring(1,only64BasedPrivateKey.length() - 1);
            byte[] encodedKeybased64 = Base64.getMimeDecoder().decode(only64BasedPrivateKey);

            PKCS8EncodedKeySpec encoded = new PKCS8EncodedKeySpec(encodedKeybased64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(encoded);
        }
        catch (BadPaddingException e) {
            java.util.Date date = new Date(System.currentTimeMillis());
            LogController.storeRegistry(4005, formatter.format(date), null, new UserModel(Login));
            JOptionPane.showMessageDialog(null, "Frase secreta inválida!","ERRO", JOptionPane.OK_OPTION);
            return null;
        }
    }

    public static void setUserCertificate(String login) throws SQLException, FileNotFoundException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();

        String query = "Update Usuario" +
                       " set certificado = ?" +
                       " where email='"+login+"' ";

        PreparedStatement statement = DbSingletonController.setPreparedStatement(query);

        FileReader reader = new FileReader("Keys/user01-x509.crt");
        statement.setCharacterStream(1, reader);
        statement.execute();
    }


    public static PublicKey getUserPublicKeyFromCertificate (String login) throws SQLException, CertificateException {
        boolean is64BasedCertificate = false;
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery(String.format("select certificado from Usuario where LoginNome='%s'", login));
        if (rs != null && rs.next()) {
            String certificado = rs.getString(1);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bytes = new ByteArrayInputStream(certificado.getBytes());
            X509Certificate certificate = (X509Certificate)cf.generateCertificate(bytes);
            return certificate.getPublicKey();
        }
        return null;
    }

    public static Object decryptFile(UserModel model, ArrayList<File> indexFiles, boolean isIndex) throws
            NoSuchAlgorithmException,
            IOException,
            IllegalBlockSizeException, SQLException, NoSuchPaddingException, SignatureException, InvalidKeyException {
        File envFile = null;
        File encFile = null;
        File asdFile = null;
        Date date;

        for(File f: indexFiles) {
            if (f.getName().contains("enc")){
                encFile = f;
            }
            else if(f.getName().contains("env")) {
                envFile = f;
            }
            else {
                asdFile = f;
            }
        }

        // decriptação do arquivo .env
        Cipher cipher = null;
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, model.getPrivateKey());


        Path envPath = Paths.get(envFile.getPath());
        byte[] newEnvPlainText = new byte[0];

        try {
            newEnvPlainText = cipher.doFinal(Files.readAllBytes(envPath));
            if(!isIndex) {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8013, formatter.format(date), envFile.getName(), model);
            }
        } catch (BadPaddingException e) {
            date = new Date(System.currentTimeMillis());
            LogController.storeRegistry(8015, formatter.format(date),envFile.getName(), model);
            return null;
        }
        String seedGenerated = new String(newEnvPlainText, "UTF8");


        // geração do PRNG
        SecureRandom pnrg = SecureRandom.getInstance("SHA1PRNG");
        pnrg.setSeed(seedGenerated.getBytes());

        // geração da K_DES para decriptar o arquivo .enc
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        keyGen.init(pnrg);
        Key key = keyGen.generateKey();

        // decriptação do arquivo .enc
        cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] newEncPlainTextBytes = new byte[0];
        try {
            newEncPlainTextBytes = cipher.doFinal(Files.readAllBytes(Paths.get(encFile.getPath())));
            if (isIndex) {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8005, formatter.format(date), null, model);
            }
            else {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8013, formatter.format(date), encFile.getName(), model);
            }
        } catch (BadPaddingException e) {
            if (isIndex) {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8007, formatter.format(date), null, model);
            }
            else {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8015, formatter.format(date), encFile.getName(), model);
            }
            return null;
        }
        String encPlainText = new String(newEncPlainTextBytes, "UTF8");


        // Verificação da assinatura digital
        Signature sig = Signature.getInstance("SHA1WithRSA");
        byte[] asdFileByteArray = Files.readAllBytes(Paths.get(asdFile.getPath()));
        try {
            sig.initVerify(model.getPublicKey());
        } catch (InvalidKeyException e) {
            date = new Date(System.currentTimeMillis());
            LogController.storeRegistry(8016, formatter.format(date), encFile.getName(), model);
            return null;
        }
        sig.update(newEncPlainTextBytes);


        try {
            if (sig.verify(asdFileByteArray)) {
                date = new Date(System.currentTimeMillis());
                if (isIndex){
                    LogController.storeRegistry(8006, formatter.format(date), null, model);
                    return encPlainText.split("\n");
                }
                else {
                    LogController.storeRegistry(8014, formatter.format(date), encFile.getName(), model);
                    return newEncPlainTextBytes;
                }
            }
            else{
                if(isIndex) {
                    date = new Date(System.currentTimeMillis());
                    LogController.storeRegistry(8008, formatter.format(date), null, model);
                }
                else {
                    date = new Date(System.currentTimeMillis());
                    LogController.storeRegistry(8016, formatter.format(date), encFile.getName(), model);
                }
                return null;
            }
        } catch (SignatureException e) {
            if(isIndex) {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8008, formatter.format(date), null, model);
            }
            else {
                date = new Date(System.currentTimeMillis());
                LogController.storeRegistry(8016, formatter.format(date), encFile.getName(), model);
            }
            return null;
        }
    }
}












