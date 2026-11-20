package cm.atlanticpalace.pressing;
import java.sql.*;
public class ShowData {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/atlantic_palace_pressing?characterEncoding=UTF-8&serverTimezone=UTC&useUnicode=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "")) {
            Statement stmt = conn.createStatement();
            stmt.execute("SET NAMES utf8mb4");
            
            System.out.println("=== PARAMETRES ===");
            ResultSet rs = stmt.executeQuery("SELECT * FROM parametre");
            while (rs.next()) {
                System.out.println("  " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4) + " | " + rs.getString(5));
            }
            
            System.out.println("\n=== PRESTATIONS ===");
            rs = stmt.executeQuery("SELECT * FROM prestation");
            while (rs.next()) System.out.println("  " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getInt(3) + "h");
            
            System.out.println("\n=== TARIFS ===");
            rs = stmt.executeQuery("SELECT t.id_tarif, p.libelle_prestation, t.prix_unitaire, t.date_debut_validite, t.date_fin_validite FROM tarif t JOIN prestation p ON t.id_prestation = p.id_prestation");
            while (rs.next()) System.out.println("  " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getBigDecimal(3) + " FCFA | " + rs.getString(4) + " -> " + rs.getString(5));
            
            System.out.println("\n=== AGENCES ===");
            rs = stmt.executeQuery("SELECT * FROM agence");
            while (rs.next()) System.out.println("  " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4) + " | " + rs.getString(5));
            
            System.out.println("\n=== UTILISATEURS ===");
            rs = stmt.executeQuery("SELECT id_utilisateur, nom_utilisateur, login_utilisateur, profil, statut_compte, id_agence FROM utilisateur");
            while (rs.next()) System.out.println("  " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4) + " | " + rs.getString(5) + " | agence=" + rs.getInt(6));
            
            System.out.println("\n=== PRODUITS ===");
            rs = stmt.executeQuery("SELECT * FROM produit");
            while (rs.next()) System.out.println("  " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4) + " | seuil=" + rs.getInt(5));
        }
    }
}