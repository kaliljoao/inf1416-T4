package Rules;

import java.util.ArrayList;
import java.util.List;

public class CtrlRules implements Observable {

    private static CtrlRules ctrl = null;
    List<Observer> lob = new ArrayList<Observer>();

    CtrlRules() {

    }

    public static CtrlRules getCtrlRules() {
        if (ctrl == null)
            ctrl = new CtrlRules();
        return ctrl;
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