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
                String sqlIns = "INSERT INTO utilisateurs (nom, login, mot_de_passe, role, actif, date_creation) VALUES (?,?,?,?,?,date('now'))";
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

    /**
     * Vérifie si c'est le premier lancement (seul le compte admin par défaut
     * existe).
     */
    public boolean isFirstLaunch() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count == 0)
                    return true;
                if (count == 1) {
                    // Vérifier si c'est le compte admin par défaut
                    String sql2 = "SELECT login, mot_de_passe FROM utilisateurs LIMIT 1";
                    try (ResultSet rs2 = stmt.executeQuery(sql2)) {
                        if (rs2.next()) {
                            return "admin".equals(rs2.getString("login"))
                                    && "admin123".equals(rs2.getString("mot_de_passe"));
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Crée un nouvel utilisateur.
     */
    public void creerUtilisateur(String nom, String login, String motDePasse, String role) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, login, mot_de_passe, role, actif, date_creation) VALUES (?,?,?,?,1,date('now'))";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, login);
            ps.setString(3, motDePasse);
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }

    /**
     * Vérifie si un login existe déjà.
     */
    public boolean loginExiste(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE login=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
