package repositories;

import dbContext.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeProductRepository {
    public static class TypeProductItem {
        public final int id;
        public final String name;
        public TypeProductItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; } // hiển thị trên JComboBox
    }

    public List<TypeProductItem> findAll() throws SQLException {
        String sql = """
            SELECT IdTypeProduct, NameType
            FROM PosFastFoods.dbo.TypeProduct
            ORDER BY NameType
        """;
        List<TypeProductItem> list = new ArrayList<>();
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TypeProductItem(
                        rs.getInt("IdTypeProduct"),
                        rs.getString("NameType")
                ));
            }
        }
        return list;
    }
}
