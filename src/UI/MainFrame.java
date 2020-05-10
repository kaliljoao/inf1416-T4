package UI;

import Rules.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.swing.*;

import com.sun.glass.events.KeyEvent;

public class MainFrame extends JFrame {

    private CtrlRules Controller = CtrlRules.getCtrlRules();
    final int LARG_DEFAULT = 400;
    final int ALT_DEFAULT = 260;

    public MainFrame(CtrlRules c) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int sl = screenSize.width;
        int sa = screenSize.height;
        int x = sl / 2 - LARG_DEFAULT / 2;
        int y = sa / 2 - ALT_DEFAULT / 2;

        setBounds(x, y, LARG_DEFAULT, ALT_DEFAULT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setTitle("Auth Application");

        Container container = new Container(this);
        this.add(container, BorderLayout.CENTER);
    }

    public static void main(String args[]) {
        MainFrame frame = new MainFrame(CtrlRules.getCtrlRules());
        frame.setVisible(true);
    }
}
