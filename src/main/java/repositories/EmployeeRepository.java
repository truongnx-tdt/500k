package repositories;

import dbContext.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeRepository {

    /** Lấy password hiện tại (plain) của employee */
    public String getPasswordById(int id) throws Exception {
        String sql = """
            SELECT [Password] 
            FROM PosFastFoods.dbo.Employee
            WHERE IdEmployee = ?
        """;
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public int updatePassword(int id, String newPassword) throws Exception {
        String sql = """
            UPDATE PosFastFoods.dbo.Employee
            SET [Password] = ?
            WHERE IdEmployee = ?
        """;
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            return ps.executeUpdate(); // trả về số dòng cập nhật (1 nếu OK)
        }
    }

    public boolean changePasswordIfMatch(int id, String currentPlain, String newPlain) throws Exception {
        String dbPass = getPasswordById(id);
        if (dbPass == null) return false;
        if (!String.valueOf(dbPass).equals(currentPlain)) return false;
        return updatePassword(id, newPlain) == 1;
    }
}
