package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.Niveau;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des niveaux scolaires.
 */
public class NiveauDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Liste tous les niveaux triés par ordre.
     */
    public List<Niveau> listerTous() throws SQLException {
        List<Niveau> liste = new ArrayList<>();
        String sql = "SELECT * FROM niveaux ORDER BY ordre";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        }
        return liste;
    }

    /**
     * Recherche de niveaux par code ou libellé.
     */
    public List<Niveau> rechercher(String query) throws SQLException {
        List<Niveau> liste = new ArrayList<>();
        String sql = "SELECT * FROM niveaux WHERE UPPER(code) LIKE UPPER(?) OR UPPER(libelle) LIKE UPPER(?) ORDER BY ordre";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, "%" + query.trim() + "%");
            ps.setString(2, "%" + query.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    liste.add(mapper(rs));
            }
        }
        return liste;
    }

    /**
     * Ajoute un nouveau niveau.
     */
    public void ajouter(Niveau n) throws SQLException {
        String sql = "INSERT INTO niveaux (code, libelle, ordre, description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getCode());
            ps.setString(2, n.getLibelle());
            ps.setInt(3, n.getOrdre());
            ps.setString(4, n.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    n.setId(rs.getInt(1));
            }
        }
    }

    /**
     * Modifie un niveau existant.
     */
    public void modifier(Niveau n) throws SQLException {
        String sql = "UPDATE niveaux SET code=?, libelle=?, ordre=?, description=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, n.getCode());
            ps.setString(2, n.getLibelle());
            ps.setInt(3, n.getOrdre());
            ps.setString(4, n.getDescription());
            ps.setInt(5, n.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Supprime un niveau par son ID.
     */
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM niveaux WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Vérifie si un code de niveau existe déjà.
     */
    public boolean codeExiste(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM niveaux WHERE code=? AND id!=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Niveau mapper(ResultSet rs) throws SQLException {
        return new Niveau(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("libelle"),
                rs.getInt("ordre"),
                rs.getString("description"));
    }
}
