package mg.ecole.modele;

/**
 * Modèle représentant un tarif scolaire (écolage, droit, etc.)
 * spécifique à un niveau et une année scolaire.
 */
public class FraisScolaire {

    private int id;
    private String anneeScolaire;
    private int niveauId;
    private String typePaiement; // DROITS, ECOLAGE, FRAM, PARTICIPATION
    private double montant;

    public FraisScolaire() {
    }

    public FraisScolaire(int id, String anneeScolaire, int niveauId, String typePaiement, double montant) {
        this.id = id;
        this.anneeScolaire = anneeScolaire;
        this.niveauId = niveauId;
        this.typePaiement = typePaiement;
        this.montant = montant;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public int getNiveauId() {
        return niveauId;
    }

    public void setNiveauId(int niveauId) {
        this.niveauId = niveauId;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }
}
