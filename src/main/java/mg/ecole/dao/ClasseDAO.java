package mg.ecole.dao;

import mg.ecole.database.DatabaseManager;
import mg.ecole.modele.Classe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des classes.
 */
public class ClasseDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Liste toutes les classes pour une année scolaire. */
    public List<Classe> listerParAnnee(String anneeScolaire) throws SQLException {
        return listerAvecFiltre(null, null, anneeScolaire);
    }

    /**
     * Recherche multicritère de classes.
     * 
     * @param nom           filtre sur le nom de la classe
     * @param niveauCode    filtre sur le code du niveau (ex: CP)
     * @param anneeScolaire filtre sur l'année scolaire
     */
    public List<Classe> listerAvecFiltre(String nom, String niveauCode, String anneeScolaire) throws SQLException {
        List<Classe> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.nom, c.niveau_id, c.annee_scolaire, c.enseignant_id, " +
                        "       c.capacite_max, c.description, " +
                        "       n.code AS niveau_code, n.libelle AS niveau_libelle, " +
                        "       (e.prenom || ' ' || e.nom) AS enseignant_nom, " +
                        "       (SELECT COUNT(*) FROM inscriptions i WHERE i.classe_id=c.id AND i.annee_scolaire=c.annee_scolaire AND i.statut='ACTIF') AS nb_eleves "
                        +
                        "FROM classes c " +
                        "LEFT JOIN niveaux n ON c.niveau_id = n.id " +
                        "LEFT JOIN enseignants e ON c.enseignant_id = e.id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (anneeScolaire != null && !anneeScolaire.trim().isEmpty()) {
            sql.append(" AND c.annee_scolaire=?");
            params.add(anneeScolaire.trim());
        }
        if (nom != null && !nom.trim().isEmpty()) {
            sql.append(" AND UPPER(c.nom) LIKE UPPER(?)");
            params.add("%" + nom.trim() + "%");
        }
        if (niveauCode != null && !niveauCode.trim().isEmpty()) {
            sql.append(" AND UPPER(n.code) LIKE UPPER(?)");
            params.add("%" + niveauCode.trim() + "%");
        }
        sql.append(" ORDER BY n.ordre, c.nom");

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

    /** Retourne une classe par son ID. */
    public Classe trouverParId(int id) throws SQLException {
        String sql = "SELECT c.id, c.nom, c.niveau_id, c.annee_scolaire, c.enseignant_id, " +
                "       c.capacite_max, c.description, " +
                "       n.code AS niveau_code, n.libelle AS niveau_libelle, " +
                "       (e.prenom || ' ' || e.nom) AS enseignant_nom, " +
                "       (SELECT COUNT(*) FROM inscriptions i WHERE i.classe_id=c.id AND i.statut='ACTIF') AS nb_eleves "
                +
                "FROM classes c " +
                "LEFT JOIN niveaux n ON c.niveau_id=n.id " +
                "LEFT JOIN enseignants e ON c.enseignant_id=e.id " +
                "WHERE c.id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapper(rs);
            }
        }
        return null;
    }

    /** Ajoute une nouvelle classe. */
    public int ajouter(Classe c) throws SQLException {
        String sql = "INSERT INTO classes (nom, niveau_id, annee_scolaire, enseignant_id, capacite_max, description) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setInt(2, c.getNiveauId());
            ps.setString(3, c.getAnneeScolaire());
            if (c.getEnseignantId() != null)
                ps.setInt(4, c.getEnseignantId());
            else
                ps.setNull(4, Types.INTEGER);
            ps.setInt(5, c.getCapaciteMax());
            ps.setString(6, c.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    c.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /** Met à jour une classe. */
    public void modifier(Classe c) throws SQLException {
        String sql = "UPDATE classes SET nom=?,niveau_id=?,annee_scolaire=?,enseignant_id=?,capacite_max=?,description=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setInt(2, c.getNiveauId());
            ps.setString(3, c.getAnneeScolaire());
            if (c.getEnseignantId() != null)
                ps.setInt(4, c.getEnseignantId());
            else
                ps.setNull(4, Types.INTEGER);
            ps.setInt(5, c.getCapaciteMax());
            ps.setString(6, c.getDescription());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        }
    }

    /** Supprime une classe. */
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM classes WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Retourne la liste des années scolaires distinctes. */
    public List<String> listerAnneesScolaires() throws SQLException {
        List<String> annees = new ArrayList<>();
        String sql = "SELECT DISTINCT annee_scolaire FROM classes ORDER BY annee_scolaire DESC";
        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                annees.add(rs.getString(1));
        }
        return annees;
    }

    private Classe mapper(ResultSet rs) throws SQLException {
        Classe c = new Classe();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setNiveauId(rs.getInt("niveau_id"));
        c.setAnneeScolaire(rs.getString("annee_scolaire"));
        int ensId = rs.getInt("enseignant_id");
        c.setEnseignantId(rs.wasNull() ? null : ensId);
        c.setCapaciteMax(rs.getInt("capacite_max"));
        c.setDescription(rs.getString("description"));
        c.setNiveauCode(rs.getString("niveau_code"));
        c.setNiveauLibelle(rs.getString("niveau_libelle"));
        c.setEnseignantNom(rs.getString("enseignant_nom"));
        c.setNombreEleves(rs.getInt("nb_eleves"));
        return c;
    }
}
