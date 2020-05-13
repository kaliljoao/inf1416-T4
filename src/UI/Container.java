package UI;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import Rules.*;
import sun.rmi.runtime.Log;

import javax.swing.*;

public class Container extends JPanel implements Observer {

    private String player1, player2;
    private MainFrame Frame;
    private CtrlRules Ctrl;
    private JButton[] arrBtns = new JButton[]{new JButton(), new JButton(), new JButton(), new JButton(), new JButton()};
    private String passwordText = "";
    private ArrayList<Object> passwordWithNumbersArray = new ArrayList<Object>();
    private String Login;


    public Container(MainFrame mainFrame) throws SQLException, ClassNotFoundException {
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
                JFileChooser fileChooser = new JFileChooser();
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
        this.add(menu);
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
