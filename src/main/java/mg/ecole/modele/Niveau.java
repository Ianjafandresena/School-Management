package mg.ecole.modele;

/**
 * Modèle représentant un niveau scolaire (TPS, PS, MS, GS, CP, CE1, CE2, CM1, CM2)
 */
public class Niveau {

    private int    id;
    private String code;
    private String libelle;
    private int    ordre;
    private String description;

    public Niveau() {}

    public Niveau(int id, String code, String libelle, int ordre, String description) {
        this.id          = id;
        this.code        = code;
        this.libelle     = libelle;
        this.ordre       = ordre;
        this.description = description;
    }

    // Getters & Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getCode()               { return code; }
    public void   setCode(String code)    { this.code = code; }

    public String getLibelle()                { return libelle; }
    public void   setLibelle(String libelle)  { this.libelle = libelle; }

    public int    getOrdre()              { return ordre; }
    public void   setOrdre(int ordre)     { this.ordre = ordre; }

    public String getDescription()                    { return description; }
    public void   setDescription(String description)  { this.description = description; }

    @Override
    public String toString() {
        return code + " - " + libelle;
    }
}
