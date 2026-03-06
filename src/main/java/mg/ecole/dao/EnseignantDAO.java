package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.Enseignant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des enseignants.
 */
public class EnseignantDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Liste tous les enseignants actifs. */
    public List<Enseignant> listerTous() throws SQLException {
        return listerAvecFiltre(null, null, null, true);
    }

    /**
     * Recherche multicritère d'enseignants.
     * 
     * @param nom       filtre sur nom ou prénom (LIKE)
     * @param matiere   filtre sur matière
     * @param sexe      filtre sur sexe (M/F ou null pour tous)
     * @param actifOnly si true, seulement les actifs
     */
    /**
     * Recherche multicritère d'enseignants.
     */
    public List<Enseignant> listerAvecFiltre(String nom, String matiere, String sexe, boolean actifOnly)
            throws SQLException {
        List<Enseignant> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.matricule, e.nom, e.prenom, e.sexe, e.telephone, e.email, " +
                        "GROUP_CONCAT(m.libelle, ', ') as matieres_liste, e.date_embauche, e.actif " +
                        "FROM enseignants e " +
                        "LEFT JOIN enseignant_matieres em ON em.enseignant_id = e.id " +
                        "LEFT JOIN matieres m ON m.id = em.matiere_id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (actifOnly) {
            sql.append(" AND e.actif=1");
        }
        if (nom != null && !nom.trim().isEmpty()) {
            sql.append(" AND (UPPER(e.nom) LIKE UPPER(?) OR UPPER(e.prenom) LIKE UPPER(?))");
            params.add("%" + nom.trim() + "%");
            params.add("%" + nom.trim() + "%");
        }
        if (sexe != null && !sexe.trim().isEmpty()) {
            sql.append(" AND e.sexe=?");
            params.add(sexe.trim());
        }

        sql.append(" GROUP BY e.id ");

        // Filtre sur la matière après le groupement ou via une sous-requête ?
        // Le plus simple en SQLite pour le filtre LIKE sur une des matières :
        if (matiere != null && !matiere.trim().isEmpty()) {
            sql.append(" HAVING UPPER(matieres_liste) LIKE UPPER(?)");
            params.add("%" + matiere.trim() + "%");
        }

        sql.append(" ORDER BY e.nom, e.prenom");

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

    /** Cherche un enseignant par son ID. */
    public Enseignant trouverParId(int id) throws SQLException {
        String sql = "SELECT e.id, e.matricule, e.nom, e.prenom, e.sexe, e.telephone, e.email, " +
                "GROUP_CONCAT(m.libelle, ', ') as matieres_liste, e.date_embauche, e.actif " +
                "FROM enseignants e " +
                "LEFT JOIN enseignant_matieres em ON em.enseignant_id = e.id " +
                "LEFT JOIN matieres m ON m.id = em.matiere_id " +
                "WHERE e.id=? GROUP BY e.id";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapper(rs);
            }
        }
        return null;
    }

    /** Ajoute un nouvel enseignant, retourne l'ID généré. */
    public int ajouter(Enseignant e) throws SQLException {
        String sql = "INSERT INTO enseignants (matricule,nom,prenom,sexe,telephone,email,date_embauche,actif) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getMatricule());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getSexe());
            ps.setString(5, e.getTelephone());
            ps.setString(6, e.getEmail());
            ps.setString(7, e.getDateEmbauche());
            ps.setInt(8, e.isActif() ? 1 : 0);
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

    /** Met à jour les informations d'un enseignant. */
    public void modifier(Enseignant e) throws SQLException {
        String sql = "UPDATE enseignants SET matricule=?,nom=?,prenom=?,sexe=?,telephone=?,email=?," +
                "date_embauche=?,actif=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, e.getMatricule());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getSexe());
            ps.setString(5, e.getTelephone());
            ps.setString(6, e.getEmail());
            ps.setString(7, e.getDateEmbauche());
            ps.setInt(8, e.isActif() ? 1 : 0);
            ps.setInt(9, e.getId());
            ps.executeUpdate();
        }
    }

    /** Désactive (suppression logique) un enseignant. */
    public void desactiver(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("UPDATE enseignants SET actif=0 WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Supprime physiquement un enseignant (si non affecté à une classe). */
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM enseignants WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Génère un matricule automatique pour les enseignants. */
    public String genererMatricule() throws SQLException {
        String sql = "SELECT COUNT(*)+1 AS prochain FROM enseignants";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return String.format("ENS%04d", rs.getInt("prochain"));
        }
        return "ENS0001";
    }

    /** Vérifie si le matricule est déjà utilisé. */
    public boolean matriculeExiste(String matricule, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enseignants WHERE matricule=? AND id!=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, matricule);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Enseignant mapper(ResultSet rs) throws SQLException {
        return new Enseignant(
                rs.getInt("id"),
                rs.getString("matricule"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("sexe"),
                rs.getString("telephone"),
                rs.getString("email"),
                rs.getString("matieres_liste"),
                rs.getString("date_embauche"),
                rs.getInt("actif") == 1);
    }
}
