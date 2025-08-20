package dbContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {
    private static String url;
    private static String user;
    private static String pass;

    static {
        try {
            Properties p = new Properties();
            p.load(DB.class.getClassLoader().getResourceAsStream("db.properties"));
            url  = p.getProperty("db.url");
            user = p.getProperty("db.user");
            pass = p.getProperty("db.password");

        } catch (Exception e) {
            throw new RuntimeException("Không đọc được db.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
