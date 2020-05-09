package UI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;


public class PasswordScreen extends JPanel {


    public void OpenMenu(Container panel) {

        JButton nextButton = new JButton("Next");


        nextButton.setPreferredSize(new Dimension(100, 30));

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(110, 70));
        menu.add(nextButton);


        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }
}
