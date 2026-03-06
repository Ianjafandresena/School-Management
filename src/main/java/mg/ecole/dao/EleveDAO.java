package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.Eleve;

import java.sql.*;
import java.util.*;

/**
 * DAO pour la gestion des élèves.
 */
public class EleveDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Recherche multicritère d'élèves avec filtre sur l'année scolaire.
     */
    public List<Eleve> listerAvecFiltre(String nom, String sexe, Integer classeId, String anneeScolaire)
            throws SQLException {
        List<Eleve> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT el.*, " +
                        "       i.id AS inscription_id, i.classe_id, i.annee_scolaire, i.statut AS statut_inscription, "
                        +
                        "       c.nom AS classe_nom, c.niveau_id " +
                        "FROM eleves el " +
                        "LEFT JOIN inscriptions i ON i.eleve_id = el.id " +
                        "LEFT JOIN classes c ON c.id = i.classe_id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (anneeScolaire != null && !anneeScolaire.trim().isEmpty()) {
            sql.append(" AND i.annee_scolaire=?");
            params.add(anneeScolaire.trim());
        }
        if (nom != null && !nom.trim().isEmpty()) {
            sql.append(" AND (UPPER(el.nom) LIKE UPPER(?) OR UPPER(el.prenom) LIKE UPPER(?))");
            params.add("%" + nom.trim() + "%");
            params.add("%" + nom.trim() + "%");
        }
        if (sexe != null && !sexe.trim().isEmpty()) {
            sql.append(" AND el.sexe=?");
            params.add(sexe.trim());
        }
        if (classeId != null) {
            sql.append(" AND i.classe_id=?");
            params.add(classeId);
        }
        sql.append(" ORDER BY el.nom, el.prenom");

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

    public List<Eleve> listerTous() throws SQLException {
        return listerAvecFiltre(null, null, null, null);
    }

    public Eleve trouverParId(int id) throws SQLException {
        String sql = "SELECT el.*, " +
                "       i.id AS inscription_id, i.classe_id, i.annee_scolaire, i.statut AS statut_inscription, " +
                "       c.nom AS classe_nom, c.niveau_id " +
                "FROM eleves el " +
                "LEFT JOIN inscriptions i ON i.eleve_id=el.id " +
                "LEFT JOIN classes c ON c.id=i.classe_id " +
                "WHERE el.id=? ORDER BY i.annee_scolaire DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapper(rs);
            }
        }
        return null;
    }

    public int ajouter(Eleve e) throws SQLException {
        String sql = "INSERT INTO eleves (matricule, nom, prenom, date_naissance, lieu_naissance, sexe, " +
                "adresse, nom_pere, tel_pere, nom_mere, tel_mere, nom_tuteur, tel_tuteur, type_tuteur, " +
                "photo, date_creation) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getMatricule());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getDateNaissance());
            ps.setString(5, e.getLieuNaissance());
            ps.setString(6, e.getSexe());
            ps.setString(7, e.getAdresse());
            ps.setString(8, e.getNomPere());
            ps.setString(9, e.getTelPere());
            ps.setString(10, e.getNomMere());
            ps.setString(11, e.getTelMere());
            ps.setString(12, e.getNomTuteur());
            ps.setString(13, e.getTelTuteur());
            ps.setString(14, e.getTypeTuteur());
            ps.setString(15, e.getPhoto());
            ps.setString(16, e.getDateCreation());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    e.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public void modifier(Eleve e) throws SQLException {
        String sql = "UPDATE eleves SET matricule=?, nom=?, prenom=?, date_naissance=?, lieu_naissance=?, " +
                "sexe=?, adresse=?, nom_pere=?, tel_pere=?, nom_mere=?, tel_mere=?, nom_tuteur=?, tel_tuteur=?, " +
                "type_tuteur=?, photo=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, e.getMatricule());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getDateNaissance());
            ps.setString(5, e.getLieuNaissance());
            ps.setString(6, e.getSexe());
            ps.setString(7, e.getAdresse());
            ps.setString(8, e.getNomPere());
            ps.setString(9, e.getTelPere());
            ps.setString(10, e.getNomMere());
            ps.setString(11, e.getTelMere());
            ps.setString(12, e.getNomTuteur());
            ps.setString(13, e.getTelTuteur());
            ps.setString(14, e.getTypeTuteur());
            ps.setString(15, e.getPhoto());
            ps.setInt(16, e.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM eleves WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void affecterClasse(int eleveId, int classeId, String anneeScolaire) throws SQLException {
        String sqlCheck = "SELECT id FROM inscriptions WHERE eleve_id=? AND annee_scolaire=?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlCheck)) {
            ps.setInt(1, eleveId);
            ps.setString(2, anneeScolaire);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int inscId = rs.getInt("id");
                    String sqlUpdate = "UPDATE inscriptions SET classe_id=?,statut='ACTIF' WHERE id=?";
                    try (PreparedStatement pu = getConn().prepareStatement(sqlUpdate)) {
                        pu.setInt(1, classeId);
                        pu.setInt(2, inscId);
                        pu.executeUpdate();
                    }
                } else {
                    String today = new java.sql.Date(System.currentTimeMillis()).toString();
                    String sqlInsert = "INSERT INTO inscriptions (eleve_id,classe_id,annee_scolaire,date_inscription,statut) VALUES (?,?,?,?,'ACTIF')";
                    try (PreparedStatement pi = getConn().prepareStatement(sqlInsert)) {
                        pi.setInt(1, eleveId);
                        pi.setInt(2, classeId);
                        pi.setString(3, anneeScolaire);
                        pi.setString(4, today);
                        pi.executeUpdate();
                    }
                }
            }
        }
    }

    public String genererMatricule(String anneeScolaire) throws SQLException {
        String anneeCode = anneeScolaire != null && anneeScolaire.length() >= 4
                ? anneeScolaire.substring(0, 4)
                : String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
        String sql = "SELECT COUNT(*)+1 AS prochain FROM eleves";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return String.format("EL%s%04d", anneeCode, rs.getInt("prochain"));
        }
        return "EL" + anneeCode + "0001";
    }

    public boolean matriculeExiste(String matricule, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM eleves WHERE matricule=? AND id!=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, matricule);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int compterEleves(String anneeScolaire) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscriptions WHERE annee_scolaire=? AND statut='ACTIF'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Retourne le nombre d'élèves par niveau pour une année donnée.
     */
    public Map<Integer, Integer> compterElevesParNiveau(String annee) throws SQLException {
        Map<Integer, Integer> counts = new java.util.HashMap<>();
        String sql = "SELECT c.niveau_id, COUNT(*) as nb " +
                "FROM inscriptions i " +
                "JOIN classes c ON c.id = i.classe_id " +
                "WHERE i.annee_scolaire=? AND i.statut='ACTIF' " +
                "GROUP BY c.niveau_id";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getInt("niveau_id"), rs.getInt("nb"));
                }
            }
        }
        return counts;
    }

    private Eleve mapper(ResultSet rs) throws SQLException {
        Eleve e = new Eleve();
        e.setId(rs.getInt("id"));
        e.setMatricule(rs.getString("matricule"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));
        e.setDateNaissance(rs.getString("date_naissance"));
        e.setLieuNaissance(rs.getString("lieu_naissance"));
        e.setSexe(rs.getString("sexe"));
        e.setAdresse(rs.getString("adresse"));

        e.setNomPere(rs.getString("nom_pere"));
        e.setTelPere(rs.getString("tel_pere"));
        e.setNomMere(rs.getString("nom_mere"));
        e.setTelMere(rs.getString("tel_mere"));
        e.setNomTuteur(rs.getString("nom_tuteur"));
        e.setTelTuteur(rs.getString("tel_tuteur"));
        e.setTypeTuteur(rs.getString("type_tuteur"));

        e.setPhoto(rs.getString("photo"));
        e.setDateCreation(rs.getString("date_creation"));

        Object inscId = rs.getObject("inscription_id");
        if (inscId != null)
            e.setInscriptionId(((Number) inscId).intValue());
        Object classeId = rs.getObject("classe_id");
        if (classeId != null)
            e.setClasseId(((Number) classeId).intValue());
        e.setAnneeScolaire(rs.getString("annee_scolaire"));
        e.setStatutInscription(rs.getString("statut_inscription"));
        e.setClasseNom(rs.getString("classe_nom"));
        Object nivId = rs.getObject("niveau_id");
        if (nivId != null)
            e.setNiveauId(((Number) nivId).intValue());
        return e;
    }
}
