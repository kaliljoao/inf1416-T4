package UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import Rules.*;

import javax.swing.*;

public class Container extends JPanel implements Observer {

    private String player1, player2;
    private MainFrame Frame;
    private CtrlRules Ctrl;

    public Container(MainFrame mainFrame) {
        this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        Frame = mainFrame;

        JTextField loginArea = new JTextField();
        loginArea.setPreferredSize(new Dimension(250, 30));
        JLabel loginLabel = new JLabel("Login:");

        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(new Dimension(100, 30));

        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(300, 110));
        menu.add(loginLabel);
        menu.add(loginArea);
        menu.add(nextButton);

        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        this.add(menu);
    }


    public void CloseItself() {
        Frame.setVisible(false);
        Frame.dispose();
    }


    @Override
    public void notify(int msg, Observable o) {

    }
}
