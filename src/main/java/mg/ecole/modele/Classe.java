package mg.ecole.modele;

/**
 * Modèle représentant une classe scolaire.
 */
public class Classe {

    private int id;
    private String nom;
    private int niveauId;
    private String anneeScolaire;
    private Integer enseignantId;
    private int capaciteMax;
    private String description;

    // Champs joints (pour l'affichage)
    private String niveauCode;
    private String niveauLibelle;
    private String enseignantNom;
    private int nombreEleves;

    public Classe() {
        this.capaciteMax = 30;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNiveauId() {
        return niveauId;
    }

    public void setNiveauId(int niveauId) {
        this.niveauId = niveauId;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public Integer getEnseignantId() {
        return enseignantId;
    }

    public void setEnseignantId(Integer enseignantId) {
        this.enseignantId = enseignantId;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Champs joints
    public String getNiveauCode() {
        return niveauCode;
    }

    public void setNiveauCode(String niveauCode) {
        this.niveauCode = niveauCode;
    }

    public String getNiveauLibelle() {
        return niveauLibelle;
    }

    public void setNiveauLibelle(String niveauLibelle) {
        this.niveauLibelle = niveauLibelle;
    }

    public String getEnseignantNom() {
        return enseignantNom;
    }

    public void setEnseignantNom(String enseignantNom) {
        this.enseignantNom = enseignantNom;
    }

    public int getNombreEleves() {
        return nombreEleves;
    }

    public void setNombreEleves(int nombreEleves) {
        this.nombreEleves = nombreEleves;
    }

    @Override
    public String toString() {
        return nom + " (" + anneeScolaire + ")";
    }
}
