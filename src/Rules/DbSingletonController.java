package Rules;

import java.sql.*;

public class DbSingletonController {
    private static DbSingletonController db = null;
    private static String connectionString = "jdbc:mysql://localhost:3306/inf1416";

    private static Connection connection;
    private static Statement stmt;
    private static ResultSet rs;


    private DbSingletonController() {
    }

    public static DbSingletonController getInstance() throws SQLException, ClassNotFoundException {
        if (db == null) {
            db = new DbSingletonController();
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        return db;
    }

    public static void createConnection() throws SQLException {
        connection = DriverManager.getConnection(connectionString, "root", "kalillyra1");
    }

    public static void createStatement() throws SQLException {
        stmt = connection.createStatement();
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        rs = stmt.executeQuery(query);
        return rs;
    }

    public static int executeUpdate(String query) throws SQLException {
        return stmt.executeUpdate(query);
    }

    public static PreparedStatement setPreparedStatement (String query) throws SQLException {
        return connection.prepareStatement(query);
    }

    public static void closeConnection() {
        try {
            connection.close();
        } catch (SQLException se) {
        }
        try {
            stmt.close();
        } catch (SQLException se) {
        }
        try {
            rs.close();
        } catch (SQLException se) {
        }
    }
}
