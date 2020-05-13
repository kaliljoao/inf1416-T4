package Rules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CtrlRules implements Observable {

    private static CtrlRules ctrl = null;
    List<Observer> lob = new ArrayList<Observer>();
    String Login;

    CtrlRules() throws SQLException, ClassNotFoundException {
        DbSingletonController.getInstance();
    }

    public static CtrlRules getCtrlRules() throws SQLException, ClassNotFoundException {
        if (ctrl == null)
            ctrl = new CtrlRules();
        return ctrl;
    }

    public void setLogin(String text) {
        this.Login = text;
        System.out.println(text);
    }

    public void addObserver(Observer o) {
        lob.add(o);
    }

    public void removeObserver(Observer o) {
        lob.remove(o);
    }

    public Object get() {
        Object dados[] = new Object[5];
        return dados;
    }
}
