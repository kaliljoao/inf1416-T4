package Rules;

import Models.UserModel;

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

    public static void storeRegistry (int messageId, String data, String arquivo, UserModel model) throws SQLException {
        DbSingletonController.createConnection();
        DbSingletonController.createStatement();
        int rowsAffected;

        if (arquivo != null && model != null) {
            rowsAffected = DbSingletonController.executeUpdate(String.format("INSERT INTO Registros (Hora, IdRegistro, UserLoginName, Arquivo) values ('%s', '%s', '%s' ,'%s');",
                    data, String.valueOf(messageId), model.getLoginNome(), arquivo
                    )
            );
        }
        else if (arquivo != null) {
            rowsAffected = DbSingletonController.executeUpdate(String.format("INSERT INTO Registros (Hora, IdRegistro, Arquivo) values ('%s', '%s', '%s');",
                    data, messageId, arquivo
                    )
            );
        }
        else if (model != null) {
            rowsAffected = DbSingletonController.executeUpdate(String.format("INSERT INTO Registros (Hora, IdRegistro, UserLoginName) values ('%s', '%s', '%s');",
                    data, messageId, model.getLoginNome()
                    )
            );
        }
        else {
            rowsAffected = DbSingletonController.executeUpdate(String.format("INSERT INTO Registros (Hora, IdRegistro) values ('%s', '%s');",
                    data, messageId
                    )
            );
        }
    }
}
