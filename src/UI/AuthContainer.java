package UI;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import Models.Grupo;
import Models.UserModel;
import Rules.*;
import sun.rmi.runtime.Log;

import javax.swing.*;

public class AuthContainer extends JPanel implements Observer {

    private String player1, player2;
    private MainFrame Frame;
    private CtrlRules Ctrl;
    private JButton[] arrBtns = new JButton[]{new JButton(), new JButton(), new JButton(), new JButton(), new JButton()};
    private String passwordText = "";
    private ArrayList<Object> passwordWithNumbersArray = new ArrayList<Object>();
    private String Login;
    private JFileChooser fileChooser = new JFileChooser();
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS");

    private int PasswordTries = 0;
    private int PrivateKeyTries = 0;

    public AuthContainer(MainFrame mainFrame) throws SQLException, ClassNotFoundException {
        AuthController.getInstance();
        LogController.getInstance();
        DbSingletonController.getInstance();

        this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        Frame = mainFrame;

        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(1001, formatter.format(date),null,null);

        changeToLoginScreen();
    }

    private void changeToLoginScreen() throws SQLException {
        JTextField loginArea = new JTextField();
        loginArea.setPreferredSize(new Dimension(200, 30));
        JLabel loginLabel = new JLabel("Login:");

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(250, 200));
        menu.add(loginLabel);
        menu.add(loginArea);

        JButton confirmBtn = new JButton("Validar");
        confirmBtn.setPreferredSize(new Dimension(250, 20));
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] response = AuthController.findUserByLogin(loginArea.getText());
                if ((boolean)response[0]) {
                    Login = loginArea.getText();

                    Date date = new Date(System.currentTimeMillis());
                    try {
                        if (!(boolean)AuthController.isBlocked(Login)){
                            LogController.storeRegistry(2003, formatter.format(date), null, new UserModel(Login));
                            date = new Date(System.currentTimeMillis());
                            LogController.storeRegistry(2002, formatter.format(date),null,new UserModel(Login));
                            changeToPasswordScreen();
                        }
                        else {
                            LogController.storeRegistry(2004, formatter.format(date), null, new UserModel(Login));
                            JOptionPane.showMessageDialog(null, "Essas credenciais estão bloqueadas temporariamente!");
                        }
                    } catch (SQLException | ParseException ex) {
                        ex.printStackTrace();
                    }

                }
                else{
                    Date date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(2005, formatter.format(date),null, new UserModel(loginArea.getText()));
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null, "Login incorreto!");
                }
            }
        });
        this.add(menu);
        this.add(confirmBtn);

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(2001, formatter.format(date),null,null);
    }

    private void changeToPasswordScreen() throws SQLException {
        Clear();

        arrBtns = new JButton[]{new JButton(), new JButton(), new JButton(), new JButton(), new JButton()};
        PasswordTries = 0;
        passwordText = "";
        passwordWithNumbersArray.clear();

        JLabel lblPassword = new JLabel("Senha:");
        JPasswordField pfPassword = new JPasswordField();
        pfPassword.setPreferredSize(new Dimension(150, 30));
        pfPassword.setEditable(false);
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(250, 200));
        menu.add(lblPassword);
        menu.add(pfPassword);
        addPasswordButtons(pfPassword, menu);

        JButton clearBtn = new JButton("Limpar senha");
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordText = "";
                pfPassword.setText(passwordText);
                passwordWithNumbersArray.clear();
            }
        });
        JButton validateBtn = new JButton("Validar");
        validateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (AuthController.validatePassword(Login, passwordWithNumbersArray)) {
                        Date date = new Date(System.currentTimeMillis());
                        LogController.storeRegistry(3003, formatter.format(date),null, new UserModel(Login));

                        date = new Date(System.currentTimeMillis());
                        LogController.storeRegistry(3002, formatter.format(date),null, new UserModel(Login));

                        changeToFileAuthenticationScreen();
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Senha incorreta!","ERRO", JOptionPane.OK_OPTION);
                        if (PasswordTries < 3)
                        {
                            PasswordTries++;
                            if (PasswordTries == 1) {
                                Date date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(3004, formatter.format(date), null, new UserModel(Login));
                            }
                            else if (PasswordTries == 2) {
                                Date date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(3005, formatter.format(date), null, new UserModel(Login));
                            }
                            else {
                                Date date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(3006, formatter.format(date), null, new UserModel(Login));

                                AuthController.blockUser(Login, formatter.format(date));

                                date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(3007, formatter.format(date), null, new UserModel(Login));

                                JOptionPane.showMessageDialog(null, "Usuário bloqueado!","Atenção", JOptionPane.OK_OPTION);

                                Clear();
                                PasswordTries = 0;
                                pfPassword.setText(passwordText);
                                passwordWithNumbersArray.clear();

                                date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(3002, formatter.format(date),null, new UserModel(Login));

                                changeToLoginScreen();
                            }
                        }

                        passwordText = "";
                        pfPassword.setText(passwordText);
                        passwordWithNumbersArray.clear();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                } catch (NoSuchAlgorithmException | ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.add(menu);
        this.add(clearBtn);
        this.add(validateBtn);

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(3001, formatter.format(date),null, new UserModel(Login));
    }

    private void changeToFileAuthenticationScreen() throws SQLException {
        Clear();
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(250, 200));
        JLabel title = new JLabel("Indique a private key:");
        menu.add(title);
        JButton btnArq = new JButton("Arquivo");
        btnArq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(null);
                File f = fileChooser.getSelectedFile();
                if (f == null) {
                    Date date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(4004, formatter.format(date),null, new UserModel(Login));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                String filename = f.getAbsolutePath();
            }
        });
        menu.add(btnArq);
        JLabel secretPhrase = new JLabel("Frase secreta:");
        menu.add(secretPhrase);
        JTextField secretPhraseArea = new JTextField();
        secretPhraseArea.setPreferredSize(new Dimension(150, 30));
        menu.add(secretPhraseArea);

        JButton btnConfirm = new JButton("Entrar");
        btnConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PrivateKey userPrivateKey = AuthController.getBased64PrivateKey(secretPhraseArea.getText(), fileChooser.getSelectedFile(), Login);
                    if(userPrivateKey == null) {

                        Date date;
                        PrivateKeyTries++;
                        if(PrivateKeyTries == 3)  {
                            date = new Date(System.currentTimeMillis());
                            LogController.storeRegistry(4007, formatter.format(date),null, new UserModel(Login));

                            AuthController.blockUser(Login, formatter.format(date));

                            JOptionPane.showMessageDialog(null, "Usuário bloqueado!","Atenção", JOptionPane.OK_OPTION);

                            Clear();
                            PrivateKeyTries = 0;
                            PasswordTries = 0;
                            passwordWithNumbersArray.clear();

                            date = new Date(System.currentTimeMillis());
                            LogController.storeRegistry(4002, formatter.format(date),null, new UserModel(Login));

                            changeToLoginScreen();
                        }
                    }
                    else {
                        PublicKey userPublicKey = AuthController.getUserPublicKeyFromCertificate(Login);

                        byte[] b = new byte[2048];
                        new SecureRandom().nextBytes(b);

                        Signature sig = Signature.getInstance("SHA1WithRSA");
                        sig.initSign(userPrivateKey);
                        sig.update(b);
                        byte[] signature = sig.sign();

                        sig.initVerify(userPublicKey);
                        sig.update(b);
                        try {
                            if (sig.verify(signature)) {
                                Date date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(4003, formatter.format(date), null, new UserModel(Login));

                                date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(4002, formatter.format(date), null, new UserModel(Login));

                                changeToAuthSystem(userPrivateKey, userPublicKey);
                            } else {
                                JOptionPane.showMessageDialog(null, "Falha na assinatura digital!", "ERRO", JOptionPane.OK_OPTION);
                                Date date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(4006, formatter.format(date), null, new UserModel(Login));
                                if (PrivateKeyTries < 3) {
                                    PrivateKeyTries++;
                                } else {
                                    date = new Date(System.currentTimeMillis());
                                    LogController.storeRegistry(4007, formatter.format(date), null, new UserModel(Login));

                                    AuthController.blockUser(Login, formatter.format(date));

                                    JOptionPane.showMessageDialog(null, "Usuário bloqueado!", "Atenção", JOptionPane.OK_OPTION);

                                    Clear();
                                    changeToLoginScreen();
                                }
                            }
                        } catch (SignatureException | ParseException se) {
                            System.out.println("Singature failed");
                        } catch (ClassNotFoundException classNotFoundException) {
                            classNotFoundException.printStackTrace();
                        }
                    }
                } catch (NoSuchAlgorithmException | NoSuchProviderException noSuchAlgorithmException) {
                    noSuchAlgorithmException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (InvalidKeyException invalidKeyException) {
                    invalidKeyException.printStackTrace();
                } catch (NoSuchPaddingException noSuchPaddingException) {
                    noSuchPaddingException.printStackTrace();
                } catch (BadPaddingException badPaddingException) {
                    JOptionPane.showMessageDialog(null, "Frase secreta ou chave privada incorreta!","ERRO", JOptionPane.OK_OPTION);
                } catch (IllegalBlockSizeException illegalBlockSizeException) {
                    illegalBlockSizeException.printStackTrace();
                } catch (InvalidKeySpecException invalidKeySpecException) {
                    invalidKeySpecException.printStackTrace();
                } catch (CertificateException certificateException) {
                    certificateException.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } catch (SignatureException signatureException) {
                    signatureException.printStackTrace();
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
            }
        });
        menu.add(btnConfirm);
        this.add(menu);

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(4001, formatter.format(date),null, new UserModel(Login));
    }

    private void changeToAuthSystem(PrivateKey userPrivateKey, PublicKey userPublicKey) throws SQLException, ClassNotFoundException {
        Clear();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int sl = screenSize.width;
        int sa = screenSize.height;
        int x = sl / 2 - 550 / 2;
        int y = sa / 2 - 550 / 2;

        JFrame systemFrame = new JFrame();
        systemFrame.setBounds(x, y, 550, 550);

        CloseItself();

        UserModel model = new UserModel();
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery(String.format("select * from Usuario where LoginNome = '%s'", this.Login));
        if (rs != null && rs.next()) {
            model.setLogin_Nome(this.Login);
            model.setNome(rs.getString(2));
            int userGrupo = rs.getInt(8);
            model.setGrupo(Grupo.fromInteger(userGrupo));
            model.setQtd_Acessos(rs.getInt(9)+1);
            model.setPrivateKey(userPrivateKey);
            model.setPublicKey(userPublicKey);
            model.setQtd_Consultas(rs.getInt(10));
        }
        DbSingletonController.closeConnection();

        SystemContainer systemContainer = new SystemContainer(systemFrame, model);
        systemFrame.add(systemContainer, BorderLayout.CENTER);
        systemFrame.setVisible(true);
    }

    private void changePasswordButtons(JPanel menu, JPasswordField pfPassword) {
        ArrayList<Integer> usedNumbers = new ArrayList<Integer>();
        ArrayList<Integer> Numbers = generateArrayNumber();
        Random rand = new Random();

        for (int i = 0; i < 5; i++) {

            int position1 = rand.nextInt(Numbers.size());
            int num1 = Numbers.get(position1);
            Numbers.remove(position1);

            int position2 = rand.nextInt(Numbers.size());
            int num2 = Numbers.get(position2);
            Numbers.remove(position2);

            usedNumbers.add(num1);
            usedNumbers.add(num2);

            arrBtns[i].setText(String.valueOf(num1) + " ou " + String.valueOf(num2));
            arrBtns[i].removeActionListener(arrBtns[i].getActionListeners()[0]);
            arrBtns[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    passwordText += ".";
                    passwordWithNumbersArray.add(new String[]{((JButton) e.getSource()).getText().split(" ou ")[0], ((JButton) e.getSource()).getText().split(" ou ")[1]});
                    pfPassword.setText(passwordText);
                    changePasswordButtons(menu, pfPassword);
                }
            });
        }
    }

    private ArrayList<Integer> generateArrayNumber() {
        ArrayList<Integer> Numbers = new ArrayList<Integer>();

        Numbers.add(0);
        Numbers.add(1);
        Numbers.add(2);
        Numbers.add(3);
        Numbers.add(4);
        Numbers.add(5);
        Numbers.add(6);
        Numbers.add(7);
        Numbers.add(8);
        Numbers.add(9);
        return Numbers;
    }

    private void addPasswordButtons(JPasswordField pfPassword, JPanel menu) {
        ArrayList<Integer> usedNumbers = new ArrayList<Integer>();
        ArrayList<Integer> Numbers = generateArrayNumber();


        Random rand = new Random();


        for (int i = 0; i < 5; i++) {

            int position1 = rand.nextInt(Numbers.size());
            int num1 = Numbers.get(position1);
            Numbers.remove(position1);

            int position2 = rand.nextInt(Numbers.size());
            int num2 = Numbers.get(position2);
            Numbers.remove(position2);

            usedNumbers.add(num1);
            usedNumbers.add(num2);

            arrBtns[i].setText(String.valueOf(num1) + " ou " + String.valueOf(num2));
            arrBtns[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    passwordText += ".";
                    passwordWithNumbersArray.add(new String[]{((JButton) e.getSource()).getText().split(" ou ")[0], ((JButton) e.getSource()).getText().split(" ou ")[1]});
                    pfPassword.setText(passwordText);
                    changePasswordButtons(menu, pfPassword);
                }
            });
            menu.add(arrBtns[i]);
        }

    }


    private void Clear() {
        this.removeAll();
        this.repaint();
        this.updateUI();
    }


    public void CloseItself() {
        Frame.setVisible(false);
        Frame.dispose();
    }


    @Override
    public void notify(int msg, Observable o) {

    }
}
