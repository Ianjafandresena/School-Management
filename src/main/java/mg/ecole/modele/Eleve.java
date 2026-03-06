package mg.ecole.modele;

/**
 * Modèle représentant un élève.
 */
public class Eleve {

    private int id;
    private String matricule;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String lieuNaissance;
    private String sexe;
    private String adresse;

    // Nouveaux champs parents (Suite migration v4)
    private String nomPere;
    private String telPere;
    private String nomMere;
    private String telMere;
    private String nomTuteur;
    private String telTuteur;
    private String typeTuteur; // Père, Mère, Tuteur, Tutrice

    // Anciens champs (gardés pour compatibilité structurelle si besoin,
    // temporairement)
    private String nomParent;
    private String telephoneParent;
    private String lienParent;

    private String photo;
    private String dateCreation;

    // Champs joints (inscription en cours)
    private Integer classeId;
    private Integer niveauId;
    private String classeNom;
    private String anneeScolaire;
    private String statutInscription;
    private Integer inscriptionId;

    public Eleve() {
        this.sexe = "M";
        this.typeTuteur = "Père";
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNomPere() {
        return nomPere;
    }

    public void setNomPere(String nomPere) {
        this.nomPere = nomPere;
    }

    public String getTelPere() {
        return telPere;
    }

    public void setTelPere(String telPere) {
        this.telPere = telPere;
    }

    public String getNomMere() {
        return nomMere;
    }

    public void setNomMere(String nomMere) {
        this.nomMere = nomMere;
    }

    public String getTelMere() {
        return telMere;
    }

    public void setTelMere(String telMere) {
        this.telMere = telMere;
    }

    public String getNomTuteur() {
        return nomTuteur;
    }

    public void setNomTuteur(String nomTuteur) {
        this.nomTuteur = nomTuteur;
    }

    public String getTelTuteur() {
        return telTuteur;
    }

    public void setTelTuteur(String telTuteur) {
        this.telTuteur = telTuteur;
    }

    public String getTypeTuteur() {
        return typeTuteur;
    }

    public void setTypeTuteur(String typeTuteur) {
        this.typeTuteur = typeTuteur;
    }

    public String getNomParent() {
        return nomParent;
    }

    public void setNomParent(String nomParent) {
        this.nomParent = nomParent;
    }

    public String getTelephoneParent() {
        return telephoneParent;
    }

    public void setTelephoneParent(String telephoneParent) {
        this.telephoneParent = telephoneParent;
    }

    public String getLienParent() {
        return lienParent;
    }

    public void setLienParent(String lienParent) {
        this.lienParent = lienParent;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Champs joints
    public Integer getClasseId() {
        return classeId;
    }

    public void setClasseId(Integer classeId) {
        this.classeId = classeId;
    }

    public Integer getNiveauId() {
        return niveauId;
    }

    public void setNiveauId(Integer niveauId) {
        this.niveauId = niveauId;
    }

    public String getClasseNom() {
        return classeNom;
    }

    public void setClasseNom(String classeNom) {
        this.classeNom = classeNom;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public String getStatutInscription() {
        return statutInscription;
    }

    public void setStatutInscription(String statutInscription) {
        this.statutInscription = statutInscription;
    }

    public Integer getInscriptionId() {
        return inscriptionId;
    }

    public void setInscriptionId(Integer inscriptionId) {
        this.inscriptionId = inscriptionId;
    }

    /** Retourne prénom + nom. */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getNomComplet();
    }
}
