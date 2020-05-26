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
import java.util.ArrayList;
import java.util.Random;
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

    public AuthContainer(MainFrame mainFrame) throws SQLException, ClassNotFoundException {
        AuthController.getInstance();
        this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        Frame = mainFrame;

        JTextField loginArea = new JTextField();
        loginArea.setPreferredSize(new Dimension(200, 30));
        JLabel loginLabel = new JLabel("Login:");

        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(250, 200));
        menu.add(loginLabel);
        menu.add(loginArea);

        JButton confirmBtn = new JButton("Validar");
        confirmBtn.setPreferredSize(new Dimension(250, 20));
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AuthController.findUserByLogin(loginArea.getText())) {
                    Login = loginArea.getText();
                    changeToPasswordScreen();
                }
            }
        });
        this.add(menu);
        this.add(confirmBtn);
    }

    private void changeToPasswordScreen() {
        Clear();
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
                        changeToFileAuthenticationScreen();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                } catch (NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                }
                changeToFileAuthenticationScreen();
            }
        });
        this.add(menu);
        this.add(clearBtn);
        this.add(validateBtn);
    }

    private void changeToFileAuthenticationScreen() {
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
                    PrivateKey userPrivateKey = AuthController.getBased64PrivateKey(secretPhraseArea.getText(), fileChooser.getSelectedFile());
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
                            changeToAuthSystem(userPrivateKey, userPublicKey);
                        } else System.out.println( "Signature failed" );
                    } catch (SignatureException se) {
                        System.out.println( "Singature failed" );
                    } catch (ClassNotFoundException classNotFoundException) {
                        classNotFoundException.printStackTrace();
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
                    badPaddingException.printStackTrace();
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
                }
            }
        });
        menu.add(btnConfirm);
        this.add(menu);
    }

    private void changeToAuthSystem(PrivateKey userPrivateKey, PublicKey userPublicKey) throws SQLException, ClassNotFoundException {
        Clear();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int sl = screenSize.width;
        int sa = screenSize.height;
        int x = sl / 2 - 550 / 2;
        int y = sa / 2 - 250 / 2;

        JFrame systemFrame = new JFrame();
        systemFrame.setBounds(x, y, 550, 280);

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
