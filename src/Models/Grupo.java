package Models;

public enum Grupo {
    Admin(1),
    Usuario(2);

    private final int gp;
    Grupo(int i) {
        gp = i;
    }

    public static Grupo fromInteger(int x) {
        switch(x) {
            case 1:
                return Admin;
            case 2:
                return Usuario;
        }
        return null;
    }

    public int getGrupo(){
        return gp;
    }
}
