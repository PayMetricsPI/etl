package infrastructure.persistence.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionFactory {
    private static final String URL = "jdbc:mysql://localhost:3306/payMetrics";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "WeLike277353#";

    public static Connection createConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to establish connection");
            throw e;
        }
    }
}