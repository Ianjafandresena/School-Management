package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import java.sql.*;

/**
 * DAO pour la gestion des utilisateurs et l'authentification.
 */
public class UtilisateurDAO {

    public interface AuthResult {
        void onSuccess(String login, String role);

        void onFailure(String message);
    }

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public boolean authentifier(String login, String password, AuthResult callback) {
        String sql = "SELECT role, nom FROM utilisateurs WHERE login=? AND mot_de_passe=? AND actif=1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    callback.onSuccess(login, rs.getString("role"));
                    return true;
                }
            }
        } catch (SQLException e) {
            callback.onFailure("Erreur BDD : " + e.getMessage());
        }
        callback.onFailure("Identifiants incorrects ou compte inactif.");
        return false;
    }

    public void creerUtilisateurInitial() throws SQLException {
        // Vérifier si un admin existe déjà
        String sqlCheck = "SELECT COUNT(*) FROM utilisateurs";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sqlCheck)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sqlIns = "INSERT INTO utilisateurs (nom, login, mot_de_passe, role, actif) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = getConn().prepareStatement(sqlIns)) {
                    ps.setString(1, "Administrateur");
                    ps.setString(2, "admin");
                    ps.setString(3, "admin123");
                    ps.setString(4, "ADMIN");
                    ps.setInt(5, 1);
                    ps.executeUpdate();
                }
            }
        }
    }
}
