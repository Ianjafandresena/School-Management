package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.Matiere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des matières.
 */
public class MatiereDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Matiere> listerToutes() throws SQLException {
        List<Matiere> liste = new ArrayList<>();
        String sql = "SELECT id, code, libelle FROM matieres ORDER BY libelle";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Matiere(rs.getInt("id"), rs.getString("code"), rs.getString("libelle")));
            }
        }
        return liste;
    }

    public List<Matiere> listerParEnseignant(int enseignantId) throws SQLException {
        List<Matiere> liste = new ArrayList<>();
        String sql = "SELECT m.id, m.code, m.libelle FROM matieres m " +
                "JOIN enseignant_matieres em ON em.matiere_id = m.id " +
                "WHERE em.enseignant_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Matiere(rs.getInt("id"), rs.getString("code"), rs.getString("libelle")));
                }
            }
        }
        return liste;
    }

    public void affecterMatieres(int enseignantId, List<Integer> matiereIds) throws SQLException {
        getConn().setAutoCommit(false);
        try {
            // Supprimer les anciennes affectations
            try (PreparedStatement ps = getConn()
                    .prepareStatement("DELETE FROM enseignant_matieres WHERE enseignant_id = ?")) {
                ps.setInt(1, enseignantId);
                ps.executeUpdate();
            }
            // Ajouter les nouvelles
            if (matiereIds != null && !matiereIds.isEmpty()) {
                try (PreparedStatement ps = getConn().prepareStatement(
                        "INSERT INTO enseignant_matieres (enseignant_id, matiere_id) VALUES (?, ?)")) {
                    for (int mid : matiereIds) {
                        ps.setInt(1, enseignantId);
                        ps.setInt(2, mid);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            getConn().commit();
        } catch (SQLException e) {
            getConn().rollback();
            throw e;
        } finally {
            getConn().setAutoCommit(true);
        }
    }
}
