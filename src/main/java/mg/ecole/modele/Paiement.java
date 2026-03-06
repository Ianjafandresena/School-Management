package mg.ecole.modele;

/**
 * Modèle représentant un paiement scolaire.
 * Types : DROITS, ECOLAGE (mensuel), FRAM, PARTICIPATION
 */
public class Paiement {

    // Mois d'écolage disponibles
    public static final String[] MOIS_ECOLAGE = {
            "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DECEMBRE",
            "JANVIER", "FEVRIER", "MARS", "AVRIL", "MAI", "JUIN"
    };

    // Types de paiement
    public static final String TYPE_DROITS = "DROITS";
    public static final String TYPE_ECOLAGE = "ECOLAGE";
    public static final String TYPE_FRAM = "FRAM";
    public static final String TYPE_PARTICIPATION = "PARTICIPATION";

    // Modes de paiement
    public static final String[] MODES = { "ESPECES", "CHEQUE", "VIREMENT", "MOBILE" };

    private int id;
    private int eleveId;
    private int classeId;
    private String anneeScolaire;
    private String typePaiement;
    private String mois; // null si non mensuel
    private double montant;
    private String datePaiement;
    private String modePaiement;
    private String reference;
    private String note;
    private Integer utilisateurId;

    // Champs joints (pour affichage)
    private String eleveNom;
    private String elevePrenom;
    private String eleveMatricule;
    private String classeNom;

    public Paiement() {
        this.modePaiement = "ESPECES";
        this.typePaiement = TYPE_ECOLAGE;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEleveId() {
        return eleveId;
    }

    public void setEleveId(int eleveId) {
        this.eleveId = eleveId;
    }

    public int getClasseId() {
        return classeId;
    }

    public void setClasseId(int classeId) {
        this.classeId = classeId;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Integer utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    // Champs joints
    public String getEleveNom() {
        return eleveNom;
    }

    public void setEleveNom(String eleveNom) {
        this.eleveNom = eleveNom;
    }

    public String getElevePrenom() {
        return elevePrenom;
    }

    public void setElevePrenom(String elevePrenom) {
        this.elevePrenom = elevePrenom;
    }

    public String getEleveMatricule() {
        return eleveMatricule;
    }

    public void setEleveMatricule(String eleveMatricule) {
        this.eleveMatricule = eleveMatricule;
    }

    public String getClasseNom() {
        return classeNom;
    }

    public void setClasseNom(String classeNom) {
        this.classeNom = classeNom;
    }

    /** Retourne le libellé complet : type + mois si applicable. */
    public String getLibelleComplet() {
        if (mois != null && !mois.isEmpty())
            return typePaiement + " - " + mois;
        return typePaiement;
    }

    /** Retourne le nom complet de l'élève. */
    public String getEleveNomComplet() {
        if (elevePrenom != null && eleveNom != null)
            return elevePrenom + " " + eleveNom;
        return "";
    }
}
