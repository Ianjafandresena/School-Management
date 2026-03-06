package mg.ecole.modele;

/**
 * Modèle représentant une année scolaire.
 */
public class AnneeScolaire {

    private int id;
    private String libelle;
    private String dateDebut;
    private String dateFin;
    private boolean active;
    private boolean archivee;

    public AnneeScolaire() {
    }

    public AnneeScolaire(int id, String libelle, String dateDebut, String dateFin,
            boolean active, boolean archivee) {
        this.id = id;
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.active = active;
        this.archivee = archivee;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isArchivee() {
        return archivee;
    }

    public void setArchivee(boolean archivee) {
        this.archivee = archivee;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
