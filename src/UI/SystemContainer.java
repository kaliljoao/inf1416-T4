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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class SystemContainer extends JPanel implements Observer {

    private JFrame Frame;
    private CtrlRules Ctrl;
    private JFileChooser fileChooser = new JFileChooser();
    private SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS");

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
        AuthController.setUserModel(model);
        builSystemUI(model);

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(5001, formatter.format(date),null, model);
    }

    private void builSystemUI ( UserModel model ) {
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(450, 550));

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
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(5002, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    changeToFormScreen(model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if(model.getGrupo() == Grupo.Admin) {
            buttonsPanel.add(btnCadastro);
        }

        JButton btnAlterPassword = new JButton("Alterar senha pessoal e certificado digital do usuário");
        btnAlterPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(5003, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    changeToAlterPasswordScreen(model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonsPanel.add(btnAlterPassword);

        JButton btnConsultarArquivos = new JButton("Consultar pasta de arquivos secretos do usuário");
        btnConsultarArquivos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(5004, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    changeToFilesConsulterScreen(model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonsPanel.add(btnConsultarArquivos);

        JButton btnExit = new JButton("Sair do sistema");
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "Deseja fechar a aplicação?","Atenção",JOptionPane.OK_CANCEL_OPTION);

                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(5005, formatter.format(date),null, model);
                    date = new Date(System.currentTimeMillis());
                    LogController.storeRegistry(9001, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                if (result == JOptionPane.OK_OPTION) {
                    try {

                        date = new Date(System.currentTimeMillis());
                        LogController.storeRegistry(9003, formatter.format(date),null, model);


                        CloseItself();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(9004, formatter.format(date),null, model);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
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

    private void changeToFilesConsulterScreen(UserModel model) throws SQLException {
        JFileChooser fodlerChooser = new JFileChooser();
        fodlerChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(550, 550));

        JLabel loginLabel = new JLabel("Login: "+model.getLoginNome() + " | ");
        JLabel grupoLabel = new JLabel("Grupo: "+model.getGrupo().toString()+ " | ");
        JLabel nomeLabel = new JLabel("Nome: "+model.getNome() + " | ");
        JLabel consultasLabel = new JLabel("Consultas: "+ Integer.toString(model.getQtd_Consultas())); // mudar para consultas vindas do banco

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
                if (fodlerChooser.getSelectedFile() == null) {
                    Date date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(8004, formatter.format(date),null, model);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
        selectFolderPanel.add(btnProcurarPasta);


        JButton btnListarArquivos = new JButton("Listar arquivos");
        btnListarArquivos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(8003, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                File[] filesInDirectory = fodlerChooser.getSelectedFile().listFiles();
                ArrayList<File> indexFiles = new ArrayList<File>();
                for(File file : filesInDirectory) {
                    if(file.getName().contains("index")){
                        indexFiles.add(file);
                    }
                }
                if(indexFiles.size() == 0){
                    date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(8004, formatter.format(date),null, model);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null, "Pasta inválida!", "Atenção", JOptionPane.OK_OPTION);
                }
                try {
                    if(indexFiles.size() == 3) {
                        String[] listFiles = (String[]) AuthController.decryptFile(model, indexFiles, true);

                        if (listFiles != null) {
                            AuthController.increaseConsultas(model.getLoginNome(), model.getQtd_Consultas()+1);
                            for (int i = 0; i < listFiles.length; i++) {
                                JLabel lblArq = new JLabel(listFiles[i]);
                                JButton btnArq = new JButton("Salvar");
                                btnArq.setPreferredSize(new Dimension(50, 20));
                                int finalI = i;
                                ArrayList<File> arqFiles = new ArrayList<File>();


                                btnArq.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {


                                        for (File file : filesInDirectory) {
                                            if (file.getName().contains(listFiles[finalI].split(" ")[0])) {
                                                arqFiles.add(file);
                                            }
                                        }

                                        Date date = new Date(System.currentTimeMillis());
                                        try {
                                            LogController.storeRegistry(8010, formatter.format(date), listFiles[finalI].split(" ")[1], model);
                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                        }

                                        try {
                                            String gpArq = listFiles[finalI].split(" ")[3];
                                            String loginNomeArq = listFiles[finalI].split(" ")[2];
                                            if (gpArq.toLowerCase().equals(model.getGrupo().toString().toLowerCase()) || loginNomeArq.equals(model.getLoginNome())) {
                                                date = new Date(System.currentTimeMillis());
                                                LogController.storeRegistry(8011, formatter.format(date), listFiles[finalI].split(" ")[1], model);

                                                byte[] retDecriptedFile = (byte[]) AuthController.decryptFile(model, arqFiles, false);
                                                if (retDecriptedFile != null) {
                                                    File file = new File(listFiles[finalI].split(" ")[1]);
                                                    FileOutputStream fos = null;

                                                    fos = new FileOutputStream(file);
                                                    fos.write(retDecriptedFile);

                                                    if (fos != null) {
                                                        fos.close();
                                                    }
                                                    JOptionPane.showMessageDialog(null, "Arquivo decriptado e salvo com sucesso!", "Atenção", JOptionPane.OK_OPTION);
                                                }
                                                else {
                                                    JOptionPane.showMessageDialog(null, "Erro de decriptação do arquivo!", "Atenção", JOptionPane.OK_OPTION);
                                                }
                                            }
                                            else {
                                                date = new Date(System.currentTimeMillis());
                                                LogController.storeRegistry(8012, formatter.format(date), listFiles[finalI].split(" ")[1], model);
                                                JOptionPane.showMessageDialog(null, "Você não tem acesso a esse arquivo!", "Atenção", JOptionPane.OK_OPTION);
                                            }

                                        } catch (NoSuchPaddingException noSuchPaddingException) {
                                            noSuchPaddingException.printStackTrace();
                                        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                            noSuchAlgorithmException.printStackTrace();
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        } catch (IllegalBlockSizeException illegalBlockSizeException) {
                                            illegalBlockSizeException.printStackTrace();
                                        } catch (SignatureException signatureException) {
                                            signatureException.printStackTrace();
                                        } catch (SQLException | InvalidKeyException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });
                                buttonsPanel.add(lblArq);
                                buttonsPanel.add(btnArq);
                            }
                            date = new Date(System.currentTimeMillis());
                            LogController.storeRegistry(8009, formatter.format(date), null, model);
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "Você não tem acesso a essa pasta!", "Atenção", JOptionPane.OK_OPTION);
                        }
                        updateUI();
                        repaint();
                    }
                } catch (NoSuchPaddingException noSuchPaddingException) {
                    noSuchPaddingException.printStackTrace();
                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                    noSuchAlgorithmException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (IllegalBlockSizeException illegalBlockSizeException) {
                    illegalBlockSizeException.printStackTrace();
                } catch (SignatureException signatureException) {
                    signatureException.printStackTrace();
                } catch (SQLException | InvalidKeyException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonsPanel.add(btnListarArquivos);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(450, 25));
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(8002, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
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

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(8001, formatter.format(date),null, model);
    }

    private void changeToFormScreen( UserModel model ) throws SQLException {
        fileChooser = new JFileChooser();
        final File[] f = new File[1];
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(450, 280));

        DbSingletonController.createConnection();
        DbSingletonController.createStatement();

        ResultSet rs = null;
        rs = DbSingletonController.executeQuery("select COUNT(*) from Usuario;");
        rs.next();


        JLabel loginLabel = new JLabel("Login: "+model.getLoginNome() + " | ");
        JLabel grupoLabel = new JLabel("Grupo: "+model.getGrupo().toString()+ " | ");
        JLabel nomeLabel = new JLabel("Nome: "+model.getNome() + " | ");
        JLabel acessosLabel = new JLabel("Total de usuários: "+ Integer.toString(rs.getInt(1)));
        GridLayout experimentLayout = new GridLayout(0,2);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(experimentLayout);

        buttonsPanel.add(new JLabel("Certificado digital:"));
        JButton btnBuscaCertificado = new JButton("Buscar Certificado Digital");
        btnBuscaCertificado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(null);
                if (fileChooser.getSelectedFile() == null) {
                    Date date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(6004, formatter.format(date),null, model);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
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
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(6007, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                Clear();
                builSystemUI(model);
            }
        });

        btnConfirmar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(6002, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
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
                        int result = JOptionPane.showConfirmDialog(null, "Este é seu certificado?\n"+registerInformations, "Confirma", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            String salt = AuthController.generateSalt();
                            String newHash = AuthController.getPasswordHash(passwordField.getText(), salt, true);
                            if (newHash != "nok") {
                                int grupoId = comboBox.getSelectedIndex() + 1;
                                int acessos = 0;
                                int isBlocked = 0;

                                boolean is64BasedCertificate = false;
                                String cert = generateCert(f[0]);


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
                                    date = new Date(System.currentTimeMillis());
                                    LogController.storeRegistry(6005, formatter.format(date),null, model);

                                    Clear();
                                    builSystemUI(model);
                                }
                                else {
                                    date = new Date(System.currentTimeMillis());
                                    LogController.storeRegistry(6006, formatter.format(date),null, model);
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

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(6001, formatter.format(date),null, model);
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

    private String generateCert(File f) throws IOException {
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
        return cert;
    }

    private void changeToAlterPasswordScreen( UserModel model ) throws SQLException {
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
                if (f[0] == null) {
                    Date date = new Date(System.currentTimeMillis());
                    try {
                        LogController.storeRegistry(7003, formatter.format(date),null, model);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
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

                            String registerInformations = String.format(
                                            "Versão Cert: %s\n" +
                                            "Série Cert: %s\n" +
                                            "Validade Cert: %s\n" +
                                            "Tipo Assinatura Cert: %s\n" +
                                            "Sujeito Cert: %s\n" +
                                            "Emissor Cert: %s\n" +
                                            "E-mail Cert: %s",
                                    certificate.getVersion(),
                                    certificate.getSerialNumber(),
                                    certificate.getNotAfter(),
                                    certificate.getSigAlgName(),
                                    certificate.getSubjectX500Principal().getName(),
                                    certificate.getIssuerX500Principal().getName(),
                                    certificate.getSubjectX500Principal().getName()
                            );

                            String query = "Update Usuario" +
                                    " set hashedPassword = '" + newHash + "'," +
                                    " salt = '" + salt + "'" +
                                    " where LoginNome='" + LoginNome + "'; ";

                            int rowAffected = DbSingletonController.executeUpdate(query);
                            DbSingletonController.closeConnection();
                            if (JOptionPane.showConfirmDialog(null, registerInformations, "Atenção", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
                                int result = JOptionPane.showConfirmDialog(null, "Senha alterada com sucesso!", "Atenção", JOptionPane.OK_CANCEL_OPTION);
                                if (result == JOptionPane.OK_OPTION) {
                                    Date date = new Date(System.currentTimeMillis());
                                    LogController.storeRegistry(7004, formatter.format(date),null, model);
                                    Clear();
                                    builSystemUI(model);
                                }
                            }
                            else {
                                Date date = new Date(System.currentTimeMillis());
                                LogController.storeRegistry(7005, formatter.format(date),null, model);
                            }
                        }
                        else {
                            Date date = new Date(System.currentTimeMillis());
                            LogController.storeRegistry(7002, formatter.format(date),null, model);
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
                Date date = new Date(System.currentTimeMillis());
                try {
                    LogController.storeRegistry(7006, formatter.format(date),null, model);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
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

        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(7001, formatter.format(date),null, model);
    }

    private void Clear() {
        this.removeAll();
        this.repaint();
        this.updateUI();
    }

    public void CloseItself() throws SQLException {
        Date date = new Date(System.currentTimeMillis());
        LogController.storeRegistry(1002, formatter.format(date),null,null);
        Frame.setVisible(false);
        Frame.dispose();
    }

    @Override
    public void notify(int msg, Observable o) {

    }
}
