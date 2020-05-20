package UI;

import Rules.*;

import java.awt.*;
import java.sql.SQLException;

import javax.swing.*;

public class MainFrame extends JFrame {

    private CtrlRules Controller = CtrlRules.getCtrlRules();
    final int LARG_DEFAULT = 300;
    final int ALT_DEFAULT = 260;

    public MainFrame(CtrlRules c) throws SQLException, ClassNotFoundException {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int sl = screenSize.width;
        int sa = screenSize.height;
        int x = sl / 2 - LARG_DEFAULT / 2;
        int y = sa / 2 - ALT_DEFAULT / 2;

        setBounds(x, y, LARG_DEFAULT, ALT_DEFAULT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setTitle("Auth Application");

        AuthContainer authContainer = new AuthContainer(this);
        this.add(authContainer, BorderLayout.CENTER);
    }

    public static void main(String args[]) throws SQLException, ClassNotFoundException {
        MainFrame frame = new MainFrame(CtrlRules.getCtrlRules());
        frame.setVisible(true);
    }
}
