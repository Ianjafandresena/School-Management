package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.FraisScolaire;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des montants des frais par niveau.
 */
public class FraisScolaireDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Récupère tous les frais configurés pour une année scolaire donnée.
     */
    public List<FraisScolaire> listerParAnnee(String annee) throws SQLException {
        List<FraisScolaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM frais_scolaires WHERE annee_scolaire = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapper(rs));
                }
            }
        }
        return liste;
    }

    /**
     * Récupère le montant spécifique pour un niveau, un type et une année.
     */
    public double getMontant(String annee, int niveauId, String typePaiement) throws SQLException {
        String sql = "SELECT montant FROM frais_scolaires WHERE annee_scolaire=? AND niveau_id=? AND type_paiement=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            ps.setInt(2, niveauId);
            ps.setString(3, typePaiement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        }
        return 0;
    }

    /**
     * Enregistre ou met à jour un montant.
     */
    public void enregistrer(FraisScolaire f) throws SQLException {
        String sql = "INSERT INTO frais_scolaires (annee_scolaire, niveau_id, type_paiement, montant) " +
                "VALUES (?,?,?,?) ON CONFLICT(annee_scolaire, niveau_id, type_paiement) DO UPDATE SET montant=excluded.montant";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, f.getAnneeScolaire());
            ps.setInt(2, f.getNiveauId());
            ps.setString(3, f.getTypePaiement());
            ps.setDouble(4, f.getMontant());
            ps.executeUpdate();
        }
    }

    private FraisScolaire mapper(ResultSet rs) throws SQLException {
        return new FraisScolaire(
                rs.getInt("id"),
                rs.getString("annee_scolaire"),
                rs.getInt("niveau_id"),
                rs.getString("type_paiement"),
                rs.getDouble("montant"));
    }
}
