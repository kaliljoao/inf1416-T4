package Models;

import java.security.PrivateKey;
import java.security.PublicKey;

public class UserModel {
    private String Nome;
    private String Login_Nome;
    private int Qtd_Acessos;
    private int Qtd_Consultas;
    private Grupo Grupo;
    private PrivateKey PrivateKey;
    private PublicKey PublicKey;

    public UserModel() {}

    public String getNome() { return Nome; }
    public String getLoginNome() { return Login_Nome; }
    public int getQtd_Acessos() { return Qtd_Acessos; }
    public int getQtd_Consultas() { return Qtd_Consultas; }
    public Grupo getGrupo() { return Grupo; }
    public PrivateKey getPrivateKey() { return PrivateKey; }
    public PublicKey getPublicKey() { return PublicKey; }

    public void setNome(String value) { this.Nome = value; }
    public void setLogin_Nome(String value) { this.Login_Nome = value; }
    public void setQtd_Acessos(int value) { this.Qtd_Acessos = value; }
    public void setQtd_Consultas(int value) { this.Qtd_Consultas = value; }
    public void setGrupo(Grupo value) { this.Grupo = value; }
    public void setPrivateKey(PrivateKey value) {this.PrivateKey = value;}
    public void setPublicKey(PublicKey value) {this.PublicKey = value;}

}
