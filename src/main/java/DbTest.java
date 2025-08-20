import dbContext.DB;

import java.sql.*;

public class DbTest {
    public static void main(String[] args) {
        try (Connection cn = DB.getConnection();
             PreparedStatement st = cn.prepareStatement("SELECT 1");
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                System.out.println("Kết nối OK: " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
