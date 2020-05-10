package UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import Rules.*;

import javax.swing.*;

public class Container extends JPanel implements Observer {

    private String player1, player2;
    private MainFrame Frame;
    private CtrlRules Ctrl;
    private JButton[] arrBtns = new JButton[]{new JButton(), new JButton(), new JButton(), new JButton(), new JButton()};
    private String passwordText = "";


    public Container(MainFrame mainFrame) {
        this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        Frame = mainFrame;

        JTextField loginArea = new JTextField();
        loginArea.setPreferredSize(new Dimension(200, 30));
        JLabel loginLabel = new JLabel("Login:");

        JLabel lblPassword = new JLabel("Senha:");
        JPasswordField pfPassword = new JPasswordField();
        pfPassword.setPreferredSize(new Dimension(150, 30));


        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(250, 200));
        menu.add(loginLabel);
        menu.add(loginArea);
        menu.add(lblPassword);
        menu.add(pfPassword);

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

        ArrayList<Integer> usedNumbers = new ArrayList<Integer>();

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
                    passwordText += String.valueOf(num1);
                    pfPassword.setText(passwordText);
                }
            });
            menu.add(arrBtns[i]);
        }
        JButton confirmBtn = new JButton("Entrar");
        confirmBtn.setPreferredSize(new Dimension(250, 20));

        this.add(menu);
        this.add(confirmBtn);
    }


    private void Clear() {
        this.removeAll();
        this.repaint();
    }


    public void CloseItself() {
        Frame.setVisible(false);
        Frame.dispose();
    }


    @Override
    public void notify(int msg, Observable o) {

    }
}
