package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.Paiement;

import java.sql.*;
import java.util.*;

/**
 * DAO pour la gestion des paiements scolaires.
 * Supporte les 4 types : DROITS, ECOLAGE (mensuel), FRAM, PARTICIPATION.
 */
public class PaiementDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    // ─── CRUD de base ────────────────────────────────────────────────────────

    /** Enregistre un paiement et retourne l'ID généré. */
    public int ajouter(Paiement p) throws SQLException {
        String sql = "INSERT INTO paiements (eleve_id,classe_id,annee_scolaire,type_paiement,mois," +
                "montant,date_paiement,mode_paiement,reference,note,utilisateur_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getEleveId());
            ps.setInt(2, p.getClasseId());
            ps.setString(3, p.getAnneeScolaire());
            ps.setString(4, p.getTypePaiement());
            ps.setString(5, p.getMois());
            ps.setDouble(6, p.getMontant());
            ps.setString(7, p.getDatePaiement());
            ps.setString(8, p.getModePaiement());
            ps.setString(9, p.getReference());
            ps.setString(10, p.getNote());
            if (p.getUtilisateurId() != null)
                ps.setInt(11, p.getUtilisateurId());
            else
                ps.setNull(11, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    p.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /** Supprime un paiement par son ID. */
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM paiements WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── Requêtes pour le suivi des paiements ────────────────────────────────

    /**
     * Liste les paiements avec filtre multicritère.
     */
    public List<Paiement> listerAvecFiltre(Integer eleveId, Integer classeId,
            String annee, String type, String mois) throws SQLException {
        List<Paiement> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.eleve_id, p.classe_id, p.annee_scolaire, p.type_paiement, p.mois, " +
                        "       p.montant, p.date_paiement, p.mode_paiement, p.reference, p.note, " +
                        "       e.nom AS eleve_nom, e.prenom AS eleve_prenom, e.matricule AS eleve_mat, " +
                        "       c.nom AS classe_nom " +
                        "FROM paiements p " +
                        "LEFT JOIN eleves e ON e.id = p.eleve_id " +
                        "LEFT JOIN classes c ON c.id = p.classe_id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (eleveId != null) {
            sql.append(" AND p.eleve_id=?");
            params.add(eleveId);
        }
        if (classeId != null) {
            sql.append(" AND p.classe_id=?");
            params.add(classeId);
        }
        if (annee != null && !annee.isEmpty()) {
            sql.append(" AND p.annee_scolaire=?");
            params.add(annee);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND p.type_paiement=?");
            params.add(type);
        }
        if (mois != null && !mois.isEmpty()) {
            sql.append(" AND p.mois=?");
            params.add(mois);
        }
        sql.append(" ORDER BY p.date_paiement DESC, p.id DESC");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    liste.add(mapper(rs));
            }
        }
        return liste;
    }

    /** Paiements d'un élève pour une année. */
    public List<Paiement> paiementsEleve(int eleveId, String annee) throws SQLException {
        return listerAvecFiltre(eleveId, null, annee, null, null);
    }

    /** Paiements d'une classe pour une année. */
    public List<Paiement> paiementsClasse(int classeId, String annee) throws SQLException {
        return listerAvecFiltre(null, classeId, annee, null, null);
    }

    // ─── Vérification des mois payés ─────────────────────────────────────────

    /**
     * Retourne la liste des mois d'écolage déjà payés pour un élève/année.
     */
    public Set<String> moisEcolagePayes(int eleveId, String annee) throws SQLException {
        Set<String> mois = new LinkedHashSet<>();
        String sql = "SELECT mois FROM paiements WHERE eleve_id=? AND annee_scolaire=? AND type_paiement='ECOLAGE' AND mois IS NOT NULL";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            ps.setString(2, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    mois.add(rs.getString("mois"));
            }
        }
        return mois;
    }

    /**
     * Vérifie si un paiement non-mensuel (DROITS, FRAM, PARTICIPATION) existe déjà
     * pour cet élève.
     */
    public boolean paiementExiste(int eleveId, String annee, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM paiements WHERE eleve_id=? AND annee_scolaire=? AND type_paiement=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            ps.setString(2, annee);
            ps.setString(3, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ─── Requêtes comptabilité / tableau Excel ────────────────────────────────

    /**
     * Retourne le total des paiements agrégé par classe et type/mois.
     * Résultat : Map<classeId → Map<"TYPE" ou "ECOLAGE|MOIS" → total>>
     */
    public Map<Integer, Map<String, Double>> totauxParClasseEtType(String annee) throws SQLException {
        Map<Integer, Map<String, Double>> result = new LinkedHashMap<>();
        String sql = "SELECT classe_id, type_paiement, mois, SUM(montant) AS total " +
                "FROM paiements WHERE annee_scolaire=? " +
                "GROUP BY classe_id, type_paiement, mois";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int classeId = rs.getInt("classe_id");
                    String type = rs.getString("type_paiement");
                    String mois = rs.getString("mois");
                    double total = rs.getDouble("total");
                    String cle = (mois != null) ? "ECOLAGE|" + mois : type;
                    result.computeIfAbsent(classeId, k -> new LinkedHashMap<>()).put(cle, total);
                }
            }
        }
        return result;
    }

    /**
     * Retourne le total déjà payé par un élève pour un type et une année (et mois).
     */
    public double getTotalPaye(int eleveId, String annee, String type, String mois) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT SUM(montant) FROM paiements WHERE eleve_id=? AND annee_scolaire=? AND type_paiement=?");
        if (mois != null)
            sql.append(" AND mois=?");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            ps.setInt(1, eleveId);
            ps.setString(2, annee);
            ps.setString(3, type);
            if (mois != null)
                ps.setString(4, mois);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /**
     * Retourne le total des paiements agrégé par NIVEAU et type/mois.
     * Résultat : Map<niveauId → Map<"TYPE" ou "ECOLAGE|MOIS" → total>>
     */
    public Map<Integer, Map<String, Double>> totauxParNiveauEtType(String annee) throws SQLException {
        Map<Integer, Map<String, Double>> result = new LinkedHashMap<>();
        String sql = "SELECT c.niveau_id, p.type_paiement, p.mois, SUM(p.montant) AS total " +
                "FROM paiements p " +
                "JOIN classes c ON c.id = p.classe_id " +
                "WHERE p.annee_scolaire=? " +
                "GROUP BY c.niveau_id, p.type_paiement, p.mois";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int niveauId = rs.getInt("niveau_id");
                    String type = rs.getString("type_paiement");
                    String mois = rs.getString("mois");
                    double total = rs.getDouble("total");
                    String cle = (mois != null) ? "ECOLAGE|" + mois : type;
                    result.computeIfAbsent(niveauId, k -> new LinkedHashMap<>()).put(cle, total);
                }
            }
        }
        return result;
    }

    /** Total des recettes par type pour une année. */
    public Map<String, Double> totalParType(String annee) throws SQLException {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT type_paiement, SUM(montant) AS total FROM paiements WHERE annee_scolaire=? GROUP BY type_paiement";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.put(rs.getString("type_paiement"), rs.getDouble("total"));
            }
        }
        return result;
    }

    /** Total général des recettes pour une année. */
    public double totalGeneral(String annee) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant),0) FROM paiements WHERE annee_scolaire=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    /** Total par classe pour un type et une année. */
    public Map<Integer, Double> totalParClasse(String annee, String type) throws SQLException {
        Map<Integer, Double> result = new LinkedHashMap<>();
        String sql = "SELECT classe_id, SUM(montant) FROM paiements WHERE annee_scolaire=? AND type_paiement=? GROUP BY classe_id";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.put(rs.getInt(1), rs.getDouble(2));
            }
        }
        return result;
    }

    // ─── Élèves non à jour (impayés) ─────────────────────────────────────────

    /**
     * Retourne les élèves n'ayant pas payé l'écolage pour un mois donné.
     * Pour cela, on cherche tous les élèves inscrits et on soustrait ceux qui ont
     * payé.
     */
    public List<Map<String, Object>> elevesImpayesEcolage(String annee, String mois) throws SQLException {
        List<Map<String, Object>> liste = new ArrayList<>();
        String sql = "SELECT e.id, e.matricule, e.nom, e.prenom, c.nom AS classe " +
                "FROM eleves e " +
                "JOIN inscriptions i ON i.eleve_id=e.id AND i.annee_scolaire=? AND i.statut='ACTIF' " +
                "JOIN classes c ON c.id=i.classe_id " +
                "WHERE e.id NOT IN (" +
                "   SELECT eleve_id FROM paiements " +
                "   WHERE annee_scolaire=? AND type_paiement='ECOLAGE' AND mois=?" +
                ") " +
                "ORDER BY c.nom, e.nom";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            ps.setString(2, annee);
            ps.setString(3, mois);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("matricule", rs.getString("matricule"));
                    row.put("nom", rs.getString("nom"));
                    row.put("prenom", rs.getString("prenom"));
                    row.put("classe", rs.getString("classe"));
                    liste.add(row);
                }
            }
        }
        return liste;
    }

    /** Génère un numéro de reçu unique (ex: REC-2026-0001). */
    public String genererNumeroRecu(String annee) throws SQLException {
        String code = annee.replace("-", "").substring(0, 4);
        String sql = "SELECT COUNT(*)+1 FROM paiements WHERE annee_scolaire=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return String.format("REC-%s-%04d", code, rs.getInt(1));
            }
        }
        return "REC-" + code + "-0001";
    }

    private Paiement mapper(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setId(rs.getInt("id"));
        p.setEleveId(rs.getInt("eleve_id"));
        p.setClasseId(rs.getInt("classe_id"));
        p.setAnneeScolaire(rs.getString("annee_scolaire"));
        p.setTypePaiement(rs.getString("type_paiement"));
        p.setMois(rs.getString("mois"));
        p.setMontant(rs.getDouble("montant"));
        p.setDatePaiement(rs.getString("date_paiement"));
        p.setModePaiement(rs.getString("mode_paiement"));
        p.setReference(rs.getString("reference"));
        p.setNote(rs.getString("note"));
        p.setEleveNom(rs.getString("eleve_nom"));
        p.setElevePrenom(rs.getString("eleve_prenom"));
        p.setEleveMatricule(rs.getString("eleve_mat"));
        p.setClasseNom(rs.getString("classe_nom"));
        return p;
    }
}
