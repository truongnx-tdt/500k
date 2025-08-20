package repositories;

import dbContext.DB;
import models.Product;
import models.TypeProduct;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ProductRepository {
	public static List<Product> findByTypeId(int typeId) throws SQLException {
		String sql = """
					SELECT *
					FROM PosFastFoods.dbo.Product
					WHERE IdTypeProduct = ?
					ORDER BY NameProduct
				""";

		List<Product> list = new ArrayList<>();
		try (Connection cn = DB.getConnection();
			 PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setInt(1, typeId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product p = new Product();
					p.setIdProduct(rs.getInt("IdProduct"));
                    p.setNameProduct(rs.getString("NameProduct"));
                    p.setDecriptions(rs.getString("Decriptions"));

					BigDecimal price = rs.getBigDecimal("PriceProduct"); // money -> BigDecimal
					p.setPriceProduct(price);

					// bit có thể null
					Boolean isActive = rs.getObject("IsActive") == null
							? null
							: rs.getObject("IsActive", Boolean.class);
					p.setIsActive(isActive);

					p.setImages(rs.getBytes("Images"));

					TypeProduct tp = new TypeProduct();
					tp.setIdTypeProduct(typeId);
					p.setTypeProduct(tp);

					list.add(p);
				}
			}
		}
		return list;
	}

	public static List<Product> searchByName(String term, int limit) throws SQLException {
		String sql = """
					SELECT *
					FROM PosFastFoods.dbo.Product
					WHERE NameProduct LIKE ?
					ORDER BY NameProduct
				""";
		List<Product> list = new ArrayList<>();
		try (Connection cn = DB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setString(1, "%" + term + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product p = new Product();
					p.setIdProduct(rs.getInt("IdProduct"));
					p.setNameProduct(rs.getString("NameProduct"));
					p.setPriceProduct(rs.getBigDecimal("PriceProduct"));
					Boolean isActive = rs.getObject("IsActive") == null ? null : rs.getObject("IsActive", Boolean.class);
					p.setIsActive(isActive);
					p.setImages(rs.getBytes("Images"));
					TypeProduct tp = new TypeProduct();
					tp.setIdTypeProduct(rs.getInt("IdTypeProduct"));
					p.setTypeProduct(tp);
					list.add(p);
				}
			}
		}
		return list;
	}

	public int insertProduct(String name, BigDecimal price, String desc,
							 byte[] imageBytes, boolean isActive,
							 int typeId, int employeeId) throws SQLException {
		String sql = """
					INSERT INTO PosFastFoods.dbo.Product
					  (NameProduct, PriceProduct, Decriptions, Images, IsActive, IdTypeProduct, IdEmployee)
					VALUES (?, ?, ?, ?, ?, ?, ?);
					SELECT SCOPE_IDENTITY();
				""";

		try (Connection cn = DB.getConnection();
			 PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setString(1, name);
			if (price != null) ps.setBigDecimal(2, price);
			else ps.setNull(2, Types.DECIMAL);
			ps.setString(3, (desc == null || desc.isBlank()) ? null : desc);
			if (imageBytes != null) ps.setBytes(4, imageBytes);
			else ps.setNull(4, Types.VARBINARY);
			ps.setBoolean(5, isActive);
			ps.setInt(6, typeId);
			ps.setInt(7, employeeId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal(1).intValue();
				}
			}
		}
		throw new SQLException("Insert failed, cannot get ID.");
	}

	public boolean updateProduct(int id, String name, BigDecimal price, String desc,
								 byte[] imageBytes, boolean isActive,
								 int typeId, int employeeId) throws SQLException {
		String sql = """
					UPDATE PosFastFoods.dbo.Product
					SET NameProduct = ?, PriceProduct = ?, Decriptions = ?, Images = ?, IsActive = ?, IdTypeProduct = ?, IdEmployee = ?
					WHERE IdProduct = ?
				""";
		try (Connection cn = DB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setString(1, name);
			if (price != null) ps.setBigDecimal(2, price); else ps.setNull(2, Types.DECIMAL);
			ps.setString(3, (desc == null || desc.isBlank()) ? null : desc);
			if (imageBytes != null) ps.setBytes(4, imageBytes); else ps.setNull(4, Types.VARBINARY);
			ps.setBoolean(5, isActive);
			ps.setInt(6, typeId);
			ps.setInt(7, employeeId);
			ps.setInt(8, id);
			return ps.executeUpdate() > 0;
		}
	}

	// Update IsActive = 0 (sold out)
	public boolean markSoldOut(int productId) {
		String sql = "UPDATE Product SET IsActive = 0 WHERE IdProduct = ?";
		try (Connection cn = DB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setInt(1, productId);
			return ps.executeUpdate() > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean markAvailable(int productId) {
		String sql = "UPDATE Product SET IsActive = 1 WHERE IdProduct = ?";
		try (Connection cn = DB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setInt(1, productId);
			return ps.executeUpdate() > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// Delete product
	public boolean deleteProduct(int productId) {
		String sql = "DELETE FROM Product WHERE IdProduct = ?";
		try (Connection cn = DB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setInt(1, productId);
			return ps.executeUpdate() > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
