package Models;

public class UserModel {
    private String Nome;
    private String Login_Nome;
    private int Qtd_Acessos;
    private Grupo Grupo;

    public UserModel() {}

    public String getNome() { return Nome; }
    public String getLoginNome() { return Login_Nome; }
    public int getQtd_Acessos() { return Qtd_Acessos; }
    public Grupo getGrupo() { return Grupo; }

    public void setNome(String value) { this.Nome = value; }
    public void setLogin_Nome(String value) { this.Login_Nome = value; }
    public void setQtd_Acessos(int value) { this.Qtd_Acessos = value; }
    public void setGrupo(Grupo value) { this.Grupo = value; }


}
