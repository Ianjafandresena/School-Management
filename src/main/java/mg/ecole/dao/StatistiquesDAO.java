package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import java.sql.*;
import java.util.*;

/**
 * DAO pour extraire des données statistiques (élèves par classe, revenus
 * mensuels, etc.).
 */
public class StatistiquesDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Nombre d'élèves inscrits par classe pour une année donnée. */
    public Map<String, Integer> nbElevesParClasse(String annee) throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT c.nom, COUNT(i.eleve_id) " +
                "FROM classes c " +
                "LEFT JOIN inscriptions i ON i.classe_id = c.id AND i.annee_scolaire=? " +
                "WHERE c.annee_scolaire=? OR c.id IN (SELECT classe_id FROM inscriptions WHERE annee_scolaire=?)" +
                "GROUP BY c.id ORDER BY c.nom";
        // Note: simplified query for robustness
        sql = "SELECT c.nom, COUNT(i.eleve_id) FROM classes c " +
                "JOIN inscriptions i ON i.classe_id=c.id " +
                "WHERE i.annee_scolaire=? GROUP BY c.id";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    stats.put(rs.getString(1), rs.getInt(2));
            }
        }
        return stats;
    }

    /** Recettes mensuelles globales pour une année donnée. */
    public Map<String, Double> recettesMensuelles(String annee) throws SQLException {
        Map<String, Double> stats = new LinkedHashMap<>();
        // On initialise tous les mois à 0.0
        String[] moisRef = { "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DECEMBRE", "JANVIER", "FEVRIER", "MARS", "AVRIL",
                "MAI", "JUIN" };
        for (String m : moisRef)
            stats.put(m, 0.0);

        String sql = "SELECT mois, SUM(montant) FROM paiements " +
                "WHERE annee_scolaire=? AND type_paiement='ECOLAGE' AND mois IS NOT NULL " +
                "GROUP BY mois";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String m = rs.getString(1);
                    if (stats.containsKey(m))
                        stats.put(m, rs.getDouble(2));
                }
            }
        }
        return stats;
    }

    /** Recettes par type pour une année donnée. */
    public Map<String, Double> recettesParType(String annee) throws SQLException {
        Map<String, Double> stats = new LinkedHashMap<>();
        String sql = "SELECT type_paiement, SUM(montant) FROM paiements WHERE annee_scolaire=? GROUP BY type_paiement";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    stats.put(rs.getString(1), rs.getDouble(2));
            }
        }
        return stats;
    }

    /** Recettes par classe pour une année donnée. */
    public Map<String, Double> recettesParClasse(String annee) throws SQLException {
        Map<String, Double> stats = new LinkedHashMap<>();
        String sql = "SELECT c.nom, SUM(p.montant) FROM paiements p " +
                "JOIN inscriptions i ON i.eleve_id = p.eleve_id AND i.annee_scolaire = p.annee_scolaire " +
                "JOIN classes c ON c.id = i.classe_id " +
                "WHERE p.annee_scolaire=? GROUP BY c.id ORDER BY c.nom";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    stats.put(rs.getString(1), rs.getDouble(2));
            }
        }
        return stats;
    }

    /** Statistiques générales pour le tableau de bord. */
    public Map<String, Object> infosDashboard(String annee) throws SQLException {
        Map<String, Object> infos = new HashMap<>();

        try (Statement stmt = getConn().createStatement()) {
            // Nb élèves
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM eleves");
            infos.put("total_eleves", rs.next() ? rs.getInt(1) : 0);

            // Nb inscrits cette année
            PreparedStatement ps = getConn()
                    .prepareStatement("SELECT COUNT(*) FROM inscriptions WHERE annee_scolaire=?");
            ps.setString(1, annee);
            rs = ps.executeQuery();
            infos.put("inscrits_annee", rs.next() ? rs.getInt(1) : 0);

            // Nb classes
            rs = stmt.executeQuery("SELECT COUNT(*) FROM classes");
            infos.put("total_classes", rs.next() ? rs.getInt(1) : 0);

            // Nb enseignants
            rs = stmt.executeQuery("SELECT COUNT(*) FROM enseignants WHERE actif=1");
            infos.put("total_enseignants", rs.next() ? rs.getInt(1) : 0);

            // Recettes totales année
            ps = getConn().prepareStatement("SELECT SUM(montant) FROM paiements WHERE annee_scolaire=?");
            ps.setString(1, annee);
            rs = ps.executeQuery();
            infos.put("recettes_annee", rs.next() ? rs.getDouble(1) : 0.0);
        }
        return infos;
    }
}
