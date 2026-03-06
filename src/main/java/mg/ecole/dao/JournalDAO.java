package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour enregistrer l'historique des actions (Journal).
 */
public class JournalDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public void log(String utilisateur, String action, String details) {
        String sql = "INSERT INTO journal (utilisateur, action, details, date_action) VALUES (?, ?, ?, datetime('now'))";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, utilisateur);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur log : " + e.getMessage());
        }
    }

    public List<String[]> listerDernieresActions(int limite) throws SQLException {
        List<String[]> logs = new ArrayList<>();
        String sql = "SELECT * FROM journal ORDER BY id DESC LIMIT " + limite;
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new String[] {
                        rs.getString("date_action"),
                        rs.getString("utilisateur"),
                        rs.getString("action"),
                        rs.getString("details")
                });
            }
        }
        return logs;
    }
}
