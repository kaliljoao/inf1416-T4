package Rules;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LogController {
    private static LogController log = null;

    private LogController() {
    }

    public static LogController getInstance() {
        if (log == null) {
            synchronized (LogController.class) {
                log = new LogController();
            }
        }
        return log;
    }

    public static void storeRegistry (int messageId, String data, String arquivo) throws SQLException {
        ResultSet rs = null;
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        rs = DbSingletonController.executeQuery("");
    }
}
