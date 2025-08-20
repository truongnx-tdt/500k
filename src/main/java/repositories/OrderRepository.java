package repositories;

import dbContext.DB;

import java.sql.*;
import java.util.Map;

public class OrderRepository {
	public int createOrder(int employeeId, int quantity, java.math.BigDecimal total,
						  Map<Integer, Integer> productIdToQty) throws SQLException {
		String insertOrder = """
				INSERT INTO PosFastFoods.dbo.[Orders] (quantity, Total, CreateDate, IdEmployee)
				VALUES (?, ?, GETDATE(), ?);
				SELECT SCOPE_IDENTITY();
			""";
		try (Connection cn = DB.getConnection();
			 PreparedStatement ps = cn.prepareStatement(insertOrder)) {
			ps.setInt(1, quantity);
			ps.setBigDecimal(2, total);
			ps.setInt(3, employeeId);
			int newId;
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) throw new SQLException("Cannot get new order id");
				newId = rs.getBigDecimal(1).intValue();
			}

			String insertDetail = "INSERT INTO PosFastFoods.dbo.OrderDetail (IdOrder, IdProduct, quantity) VALUES (?, ?, ?)";
			try (PreparedStatement ds = cn.prepareStatement(insertDetail)) {
				for (Map.Entry<Integer, Integer> e : productIdToQty.entrySet()) {
					ds.setInt(1, newId);
					ds.setInt(2, e.getKey());
					ds.setInt(3, e.getValue());
					ds.addBatch();
				}
				ds.executeBatch();
			}

			return newId;
		}
	}
}
