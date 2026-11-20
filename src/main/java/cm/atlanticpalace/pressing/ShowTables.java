package cm.atlanticpalace.pressing;
import java.sql.*;
public class ShowTables {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/atlantic_palace_pressing?characterEncoding=UTF-8&serverTimezone=UTC&useUnicode=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET NAMES utf8mb4");
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                String table = rs.getString(1);
                System.out.println("\n=== " + table + " ===");
                ResultSet cols = stmt.executeQuery("DESCRIBE " + table);
                while (cols.next()) {
                    System.out.println("  " + cols.getString("Field") + " | " + cols.getString("Type") + " | " + cols.getString("Null") + " | " + cols.getString("Key"));
                }
            }
        }
    }
}