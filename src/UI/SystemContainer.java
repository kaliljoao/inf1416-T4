package UI;

import Models.Grupo;
import Models.UserModel;
import Rules.*;
import sun.jvm.hotspot.ui.ObjectHistogramPanel;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
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

    public SystemContainer(JFrame frame, UserModel model) throws SQLException, ClassNotFoundException {
        AuthController.getInstance();
        Ctrl = CtrlRules.getCtrlRules();
        Ctrl.addObserver(this);
        this.Frame = frame;
        this.setAlignmentX(JComponent.CENTER_ALIGNMENT);


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

        if(model.getGrupo() == Grupo.Admin) {
            buttonsPanel.add(btnCadastro);
        }

        JButton btnAlterPassword = new JButton("Alterar senha pessoal e certificado digital do usuário");
        btnAlterPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToAlterPasswordScreen(model);
            }
        });
        buttonsPanel.add(btnAlterPassword);

        JButton btnConsultarArquivos = new JButton("Consultar pasta de arquivos secretos do usuário");
        btnConsultarArquivos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToFilesConsulterScreen(model);
            }
        });
        buttonsPanel.add(btnConsultarArquivos);

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

    private void changeToFilesConsulterScreen(UserModel model) {
        JFileChooser fodlerChooser = new JFileChooser();
        fodlerChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(550, 280));

        JLabel loginLabel = new JLabel("Login: "+model.getLoginNome() + " | ");
        JLabel grupoLabel = new JLabel("Grupo: "+model.getGrupo().toString()+ " | ");
        JLabel nomeLabel = new JLabel("Nome: "+model.getNome() + " | ");
        JLabel consultasLabel = new JLabel("Consultas: "+ Integer.toString(1)); // mudar para consultas vindas do banco

        GridLayout experimentLayout = new GridLayout(0,1);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(experimentLayout);

        JPanel selectFolderPanel = new JPanel();
        selectFolderPanel.setLayout(new GridLayout(0,1));
        selectFolderPanel.add(new JLabel("Selecione a pasta dos arquivos:"));

        JButton btnProcurarPasta = new JButton("Buscar");
        btnProcurarPasta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fodlerChooser.showOpenDialog(null);
            }
        });
        selectFolderPanel.add(btnProcurarPasta);


        JButton btnListarArquivos = new JButton("Listar arquivos");
        btnListarArquivos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File[] filesInDirectory = fodlerChooser.getSelectedFile().listFiles();
                ArrayList<File> indexFiles = new ArrayList<File>();
                for(File file : filesInDirectory) {
                    if(file.getName().contains("index")){
                        indexFiles.add(file);
                    }
                }
                try {
                    String[] listFiles = (String[])AuthController.decryptFile(model, indexFiles, true);

                    for(int i = 0; i < listFiles.length; i++) {
                        JLabel lblArq = new JLabel(listFiles[i]);
                        JButton btnArq = new JButton("Salvar");
                        btnArq.setPreferredSize(new Dimension(50,20));
                        int finalI = i;
                        ArrayList<File> arqFiles = new ArrayList<File>();
                        btnArq.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                for(File file : filesInDirectory) {
                                    if(file.getName().contains(listFiles[finalI].split(" ")[0])){
                                        arqFiles.add(file);
                                    }
                                }
                                try {
                                    byte[] retDecriptedFile = (byte[])AuthController.decryptFile(model, arqFiles, false);
                                    if(retDecriptedFile != null) {
                                        File file = new File(listFiles[finalI].split(" ")[1]);
                                        FileOutputStream fos = null;

                                        fos = new FileOutputStream(file);
                                        fos.write(retDecriptedFile);

                                        if (fos != null) {
                                            fos.close();
                                        }
                                    }

                                } catch (NoSuchPaddingException noSuchPaddingException) {
                                    noSuchPaddingException.printStackTrace();
                                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                    noSuchAlgorithmException.printStackTrace();
                                } catch (InvalidKeyException invalidKeyException) {
                                    invalidKeyException.printStackTrace();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                } catch (BadPaddingException badPaddingException) {
                                    badPaddingException.printStackTrace();
                                } catch (IllegalBlockSizeException illegalBlockSizeException) {
                                    illegalBlockSizeException.printStackTrace();
                                } catch (SignatureException signatureException) {
                                    signatureException.printStackTrace();
                                }
                            }
                        });
                        buttonsPanel.add(lblArq);
                        buttonsPanel.add(btnArq);
                    }
                    updateUI();
                    repaint();
                } catch (NoSuchPaddingException noSuchPaddingException) {
                    noSuchPaddingException.printStackTrace();
                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                    noSuchAlgorithmException.printStackTrace();
                } catch (InvalidKeyException invalidKeyException) {
                    invalidKeyException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (BadPaddingException badPaddingException) {
                    badPaddingException.printStackTrace();
                } catch (IllegalBlockSizeException illegalBlockSizeException) {
                    illegalBlockSizeException.printStackTrace();
                } catch (SignatureException signatureException) {
                    signatureException.printStackTrace();
                }
            }
        });
        buttonsPanel.add(btnListarArquivos);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(450, 25));
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clear();
                builSystemUI(model);
            }
        });

        menu.add(loginLabel, BorderLayout.PAGE_START);
        menu.add(grupoLabel, BorderLayout.PAGE_START);
        menu.add(nomeLabel, BorderLayout.PAGE_START);
        menu.add(consultasLabel, BorderLayout.PAGE_START);
        menu.add(selectFolderPanel, BorderLayout.CENTER);
        menu.add(buttonsPanel, BorderLayout.CENTER);

        menu.add(btnCancelar, BorderLayout.SOUTH);

        this.Clear();
        this.add(menu);
        this.updateUI();
        this.repaint();
    }

    private void changeToFormScreen( UserModel model ) {
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
                    f[0] = fileChooser.getSelectedFile();
                    X509Certificate certificate = generateCertificate(f[0]);
                    if (Arrays.equals(passwordField.getPassword(), passwordFieldConfirmation.getPassword())) {
                        String registerInformations = String.format(
                                "Grupo: %s\n" +
                                        "Versão Cert: %s\n" +
                                        "Série Cert: %s\n" +
                                        "Validade Cert: %s\n" +
                                        "Tipo Assinatura Cert: %s\n" +
                                        "Sujeito Cert: %s\n" +
                                        "Emissor Cert: %s\n" +
                                        "E-mail Cert: %s",
                                Grupo.fromInteger(comboBox.getSelectedIndex() + 1),
                                certificate.getVersion(),
                                certificate.getSerialNumber(),
                                certificate.getNotAfter(),
                                certificate.getSigAlgName(),
                                certificate.getSubjectX500Principal().getName(),
                                certificate.getIssuerX500Principal().getName(),
                                certificate.getSubjectX500Principal().getName()
                                );
                        int result = JOptionPane.showConfirmDialog(null, registerInformations, "Atenção", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            String salt = AuthController.generateSalt();
                            String newHash = AuthController.getPasswordHash(passwordField.getPassword().toString(), salt, true);
                            if (newHash != "nok") {
                                int grupoId = comboBox.getSelectedIndex() + 1;
                                int acessos = 0;
                                int isBlocked = 0;

                                boolean is64BasedCertificate = false;
                                String cert = "";


                                String LoginNome = certificate.getSubjectDN().getName().split(",")[0].split("=")[1];
                                String Nome = LoginNome.split("@")[0];

                                DbSingletonController.createConnection();
                                DbSingletonController.createStatement();

                                String query = "INSERT INTO Usuario (Nome, LoginNome, hashedPassword, salt, isBlocked, certificado, GrupoId, Acessos) VALUES" +
                                        String.format("('%s','%s','%s','%s','%d','%s','%d','%d');", Nome, LoginNome, newHash, salt, 0, cert, grupoId, 0);

                                int rowsAffected = DbSingletonController.executeUpdate(query);
                                DbSingletonController.closeConnection();

                                int resultConfirm = JOptionPane.showConfirmDialog(null, "Usuário criado com sucesso!", "Atenção", JOptionPane.OK_OPTION);
                                if (resultConfirm == JOptionPane.OK_OPTION) {
                                    Clear();
                                    builSystemUI(model);
                                }
                            }
                            else{
                                JOptionPane.showConfirmDialog(null, "Senha com números consecutivos!", "Senha inválida", JOptionPane.OK_OPTION);
                            }
                        }
                    }
                    } catch (CertificateException exception) {
                    exception.printStackTrace();
                } catch (IOException exception) {
                    exception.printStackTrace();
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
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

    private X509Certificate generateCertificate(File f) throws CertificateException, IOException {
        boolean is64BasedCertificate = false;
        String cert = "";
        BufferedReader br = new BufferedReader(new FileReader(f));
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
        return  (X509Certificate) cf.generateCertificate(bytes);
    }

    private void changeToAlterPasswordScreen( UserModel model ) {
        JPanel menu = new JPanel();
        final File[] f = new File[1];
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
                f[0] = fileChooser.getSelectedFile();
                String filename = f[0].getAbsolutePath();
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
                    String salt = AuthController.generateSalt();

                    try {
                        String newHash = AuthController.getPasswordHash(passwordField.getText(), String.valueOf(salt), true);
                        if (newHash != "nok") {
                            DbSingletonController.createConnection();
                            DbSingletonController.createStatement();
                            X509Certificate certificate = generateCertificate(f[0]);
                            String LoginNome = certificate.getSubjectDN().getName().split(",")[0].split("=")[1];

                            String query = "Update Usuario" +
                                    " set hashedPassword = '" + newHash + "'," +
                                    " salt = '" + salt + "'" +
                                    " where LoginNome='" + LoginNome + "'; ";

                            int rowAffected = DbSingletonController.executeUpdate(query);
                            DbSingletonController.closeConnection();
                            int result = JOptionPane.showConfirmDialog(null, "Usuário alterado com sucesso!", "Atenção", JOptionPane.OK_CANCEL_OPTION);
                            if (result == JOptionPane.OK_OPTION) {
                                Clear();
                                builSystemUI(model);
                            }
                        }
                        else {
                            JOptionPane.showConfirmDialog(null, "Senha com números consecutivos!", "Senha inválida", JOptionPane.OK_OPTION);
                        }
                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                        unsupportedEncodingException.printStackTrace();
                    }  catch (SQLException throwables) {
                        throwables.printStackTrace();
                    } catch (CertificateException certificateException) {
                        certificateException.printStackTrace();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else {
                    JOptionPane.showConfirmDialog(null, "Os campos SENHA e CONFIRMA SENHA não correspondem.", "Senha inválida", JOptionPane.OK_OPTION);
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
