package cm.atlanticpalace.pressing;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ShowTables2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/atlantic_palace_pressing?characterEncoding=UTF-8&serverTimezone=UTC&useUnicode=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "")) {
            Statement stmt1 = conn.createStatement();
            stmt1.execute("SET NAMES utf8mb4");
            ResultSet rs = stmt1.executeQuery("SHOW TABLES");
            List<String> tables = new ArrayList<>();
            while (rs.next()) tables.add(rs.getString(1));
            rs.close();
            for (String t : tables) {
                System.out.println("\n=== " + t + " ===");
                Statement stmt2 = conn.createStatement();
                ResultSet cols = stmt2.executeQuery("DESCRIBE `" + t + "`");
                while (cols.next()) {
                    System.out.println("  " + cols.getString("Field") + " | " + cols.getString("Type") + " | " + cols.getString("Key"));
                }
                cols.close();
            }
        }
    }
}