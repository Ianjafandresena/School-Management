package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.AnneeScolaire;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des années scolaires et des paramètres.
 */
public class AnneeScolaireDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<AnneeScolaire> lister() throws SQLException {
        List<AnneeScolaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM annees_scolaires ORDER BY libelle DESC";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new AnneeScolaire(
                        rs.getInt("id"),
                        rs.getString("libelle"),
                        rs.getString("date_debut"),
                        rs.getString("date_fin"),
                        rs.getInt("active") == 1,
                        rs.getInt("archivee") == 1));
            }
        }
        return liste;
    }

    public void ajouter(AnneeScolaire a) throws SQLException {
        String sql = "INSERT INTO annees_scolaires (libelle, date_debut, date_fin, active, archivee) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, a.getLibelle());
            ps.setString(2, a.getDateDebut());
            ps.setString(3, a.getDateFin());
            ps.setInt(4, a.isActive() ? 1 : 0);
            ps.setInt(5, a.isArchivee() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void activer(int id) throws SQLException {
        // Désactiver toutes les autres
        getConn().createStatement().executeUpdate("UPDATE annees_scolaires SET active=0");
        // Activer celle-là
        try (PreparedStatement ps = getConn().prepareStatement("UPDATE annees_scolaires SET active=1 WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        // Mettre à jour dans les paramètres aussi
        String sqlSelect = "SELECT libelle FROM annees_scolaires WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlSelect)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                majParametre("annee_scolaire", rs.getString(1));
            }
        }
    }

    // ─── Gestion des paramètres ──────────────────────────────────────────────

    public String getParametre(String cle, String defaut) throws SQLException {
        String sql = "SELECT valeur FROM parametres WHERE cle=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cle);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString(1);
        }
        return defaut;
    }

    public void majParametre(String cle, String valeur) throws SQLException {
        String sql = "INSERT OR REPLACE INTO parametres (cle, valeur) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cle);
            ps.setString(2, valeur);
            ps.executeUpdate();
        }
    }
}
