package UI;

import Models.Grupo;
import Models.UserModel;
import Rules.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemContainer extends JPanel implements Observer {

    private MainFrame Frame;
    private CtrlRules Ctrl;
    private JFileChooser fileChooser = new JFileChooser();

    public SystemContainer(MainFrame mainFrame, String login) throws SQLException, ClassNotFoundException {
        AuthController.getInstance();
        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);
        this.Frame = mainFrame;
        this.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        UserModel model = new UserModel();
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery(String.format("select * from Usuario where LoginNome = '%s'", login));
        if (rs != null && rs.next()) {
            model.setLogin_Nome(login);
            model.setNome(rs.getString(2));
            int x = rs.getInt(8);
            model.setGrupo(Grupo.fromInteger(x-1));
            model.setQtd_Acessos(rs.getInt(9));
        }
        DbSingletonController.closeConnection();

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(450, 280));

        JLabel loginLabel = new JLabel("Login: "+model.getLoginNome() + " | ");
        JLabel grupoLabel = new JLabel("Grupo: "+model.getGrupo().toString()+ " | ");
        JLabel nomeLabel = new JLabel("Nome: "+model.getNome() + " | ");
        JLabel acessosLabel = new JLabel("Acessos: "+ Integer.toString(model.getQtd_Acessos()));
        GridLayout experimentLayout = new GridLayout(0,1);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(experimentLayout);

        JButton btnCadastro = new JButton("Cadastrar novo usuário");
        btnCadastro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToFormScreen(model);
            }
        });

        buttonsPanel.add(btnCadastro);
        buttonsPanel.add(new JButton("Alterar senha pessoal e certificado digital do usuário"));
        buttonsPanel.add(new JButton("Consultar pasta de arquivos secretos do usuário"));
        buttonsPanel.add(new JButton("Sair do sistema"));

        menu.add(loginLabel, BorderLayout.PAGE_START);
        menu.add(grupoLabel, BorderLayout.PAGE_START);
        menu.add(nomeLabel, BorderLayout.PAGE_START);
        menu.add(acessosLabel, BorderLayout.PAGE_START);
        menu.add(buttonsPanel, BorderLayout.CENTER);
        this.add(menu);
        this.updateUI();
        this.repaint();
    }

    public void changeToFormScreen( UserModel model ) {
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(450, 280));

        JLabel loginLabel = new JLabel("Login: "+model.getLoginNome() + " | ");
        JLabel grupoLabel = new JLabel("Grupo: "+model.getGrupo().toString()+ " | ");
        JLabel nomeLabel = new JLabel("Nome: "+model.getNome() + " | ");
        JLabel acessosLabel = new JLabel("Acessos: "+ Integer.toString(model.getQtd_Acessos()));
        GridLayout experimentLayout = new GridLayout(0,2);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(experimentLayout);

        buttonsPanel.add(new JLabel("Certificado digital:"));
        JButton btnBuscaCertificado = new JButton("Buscar Certificado Digital");
        btnBuscaCertificado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(null);
                File f = fileChooser.getSelectedFile();
                String filename = f.getAbsolutePath();
            }
        });
        buttonsPanel.add(btnBuscaCertificado);

        buttonsPanel.add(new JLabel("Grupo do novo usuario:"));
        buttonsPanel.add(new JComboBox(new String[] {"Administrador", "Usuario"}));

        buttonsPanel.add(new JLabel("Senha:"));
        buttonsPanel.add(new JPasswordField());

        buttonsPanel.add(new JLabel("Contirmar senha:"));
        buttonsPanel.add(new JPasswordField());

        menu.add(loginLabel, BorderLayout.PAGE_START);
        menu.add(grupoLabel, BorderLayout.PAGE_START);
        menu.add(nomeLabel, BorderLayout.PAGE_START);
        menu.add(acessosLabel, BorderLayout.PAGE_START);
        menu.add(buttonsPanel, BorderLayout.CENTER);

        JButton btnConfirmar = new JButton("Confirmar");

        menu.add(btnConfirmar, BorderLayout.SOUTH);

        this.Clear();
        this.add(menu);
        this.updateUI();
        this.repaint();
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
