package UI;

import Models.Grupo;
import Models.UserModel;
import Rules.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SystemContainer extends JPanel implements Observer {

    private JFrame Frame;
    private CtrlRules Ctrl;
    private JFileChooser fileChooser = new JFileChooser();

    public SystemContainer(JFrame frame, String login) throws SQLException, ClassNotFoundException {
        AuthController.getInstance();
        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);
        this.Frame = frame;
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
            model.setGrupo(Grupo.fromInteger(x));
            model.setQtd_Acessos(rs.getInt(9)+1);
        }
        DbSingletonController.closeConnection();

        DbSingletonController.createConnection();
        DbSingletonController.createStatement();

        String query = "Update Usuario" +
                " set Acessos = "+ String.valueOf(model.getQtd_Acessos()) +
                " where LoginNome='"+ model.getLoginNome() +"'; ";

        int rowAffected = DbSingletonController.executeUpdate(query);
        DbSingletonController.closeConnection();

        builSystemUI(model);
    }

    private void builSystemUI ( UserModel model ) {
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

        JButton btnAlterPassword = new JButton("Alterar senha pessoal e certificado digital do usuário");
        btnAlterPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToAlterPasswordScreen(model);
            }
        });
        buttonsPanel.add(btnAlterPassword);


        buttonsPanel.add(new JButton("Consultar pasta de arquivos secretos do usuário"));

        JButton btnExit = new JButton("Sair do sistema");
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "Deseja fechar a aplicação?","Atenção",JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    CloseItself();
                }
            }
        });
        buttonsPanel.add(btnExit);

        menu.add(loginLabel, BorderLayout.PAGE_START);
        menu.add(grupoLabel, BorderLayout.PAGE_START);
        menu.add(nomeLabel, BorderLayout.PAGE_START);
        menu.add(acessosLabel, BorderLayout.PAGE_START);
        menu.add(buttonsPanel, BorderLayout.CENTER, menu.getComponentCount());
        this.add(menu);
        this.updateUI();
        this.repaint();
    }

    public void changeToFormScreen( UserModel model ) {
        fileChooser = new JFileChooser();
        final File[] f = new File[1];
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
            }
        });
        buttonsPanel.add(btnBuscaCertificado);

        buttonsPanel.add(new JLabel("Grupo do novo usuario:"));
        JComboBox comboBox = new JComboBox(new String[] {"Administrador", "Usuario"});
        buttonsPanel.add(comboBox);

        buttonsPanel.add(new JLabel("Senha:"));
        JPasswordField passwordField = new JPasswordField();
        buttonsPanel.add(passwordField);

        buttonsPanel.add(new JLabel("Contirmar senha:"));
        JPasswordField passwordFieldConfirmation = new JPasswordField();
        buttonsPanel.add(passwordFieldConfirmation);

        menu.add(loginLabel, BorderLayout.PAGE_START);
        menu.add(grupoLabel, BorderLayout.PAGE_START);
        menu.add(nomeLabel, BorderLayout.PAGE_START);
        menu.add(acessosLabel, BorderLayout.PAGE_START);
        menu.add(buttonsPanel, BorderLayout.CENTER);

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clear();
                builSystemUI(model);
            }
        });

        btnConfirmar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (Arrays.equals(passwordField.getPassword(), passwordFieldConfirmation.getPassword())) {
                        Random rand = new Random();
                        int salt = rand.nextInt();
                        if (salt < 0) {
                            salt = salt * (-1);
                        }
                        String newHash = AuthController.getPasswordHash(passwordField.getPassword().toString(), salt);
                        int grupoId = comboBox.getSelectedIndex() + 1;
                        int acessos = 0;
                        int isBlocked = 0;

                        boolean is64BasedCertificate = false;
                        String cert = "";

                        f[0] = fileChooser.getSelectedFile();
                        BufferedReader br = new BufferedReader(new FileReader(f[0]));
                        String st;
                        while ((st = br.readLine()) != null) {
                            if (st.equals("-----BEGIN CERTIFICATE-----")) {
                                is64BasedCertificate = true;
                                cert += st + "\n";
                                continue;
                            }
                            if (is64BasedCertificate == true) {
                                cert += st + "\n";
                                continue;
                            }
                        }
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        ByteArrayInputStream bytes = new ByteArrayInputStream(cert.getBytes());
                        X509Certificate certificate = (X509Certificate) cf.generateCertificate(bytes);

                        String LoginNome = certificate.getSubjectDN().getName().split(",")[0].split("=")[1];
                        String Nome = LoginNome.split("@")[0];

                        DbSingletonController.createConnection();
                        DbSingletonController.createStatement();

                        String query = "INSERT INTO Usuario (Nome, LoginNome, hashedPassword, salt, isBlocked, certificado, GrupoId, Acessos) VALUES" +
                                String.format("('%s','%s','%s','%s','%d','%s','%d','%d');",Nome, LoginNome, newHash, salt, 0, cert, grupoId, 0);

                        int rowsAffected = DbSingletonController.executeUpdate(query);
                        DbSingletonController.closeConnection();

                        int result = JOptionPane.showConfirmDialog(null, "Usuário criado com sucesso!","Atenção",JOptionPane.OK_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            Clear();
                            builSystemUI(model);
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                    noSuchAlgorithmException.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } catch (CertificateException certificateException) {
                    certificateException.printStackTrace();
                }
            }
        });

        menu.add(btnConfirmar, BorderLayout.SOUTH);
        menu.add(btnCancelar, BorderLayout.SOUTH);

        this.Clear();
        this.add(menu);
        this.updateUI();
        this.repaint();
    }

    public void changeToAlterPasswordScreen( UserModel model ) {
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

        buttonsPanel.add(new JLabel("Nova senha:"));
        JPasswordField passwordField = new JPasswordField();
        buttonsPanel.add(passwordField);

        buttonsPanel.add(new JLabel("Contirmar senha:"));
        JPasswordField passwordFieldConfirmation = new JPasswordField();
        buttonsPanel.add(passwordFieldConfirmation);

        menu.add(loginLabel, BorderLayout.PAGE_START);
        menu.add(grupoLabel, BorderLayout.PAGE_START);
        menu.add(nomeLabel, BorderLayout.PAGE_START);
        menu.add(acessosLabel, BorderLayout.PAGE_START);
        menu.add(buttonsPanel, BorderLayout.CENTER);

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");

        btnConfirmar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if( Arrays.equals(passwordField.getPassword(), passwordFieldConfirmation.getPassword()) ) {
                    Random rand = new Random();
                    int salt = rand.nextInt();
                    if(salt < 0) {
                        salt = salt*(-1);
                    }
                    try {
                        String newHash = AuthController.getPasswordHash(passwordField.getPassword().toString(), salt);
                        DbSingletonController.createConnection();
                        DbSingletonController.createStatement();

                        String query = "Update Usuario" +
                                " set hashedPassword = '"+ newHash +"'," +
                                " salt = '"+ String.valueOf(salt) +"'" +
                                " where LoginNome='"+ model.getLoginNome() +"'; ";

                        int rowAffected = DbSingletonController.executeUpdate(query);
                        DbSingletonController.closeConnection();
                        int result = JOptionPane.showConfirmDialog(null, "Usuário alterado com sucesso!","Atenção",JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            Clear();
                            builSystemUI(model);
                        }


                    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                        noSuchAlgorithmException.printStackTrace();
                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                        unsupportedEncodingException.printStackTrace();
                    }  catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clear();
                builSystemUI(model);
            }
        });

        menu.add(btnConfirmar, BorderLayout.SOUTH);
        menu.add(btnCancelar, BorderLayout.SOUTH);

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
