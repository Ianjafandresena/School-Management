package mg.ecole.util;

import java.awt.*;

/**
 * Constantes globales de l'application.
 */
public class Constantes {

    // Informations application
    public static final String NOM_APP = "Gestion École Primaire";
    public static final String VERSION = "1.0.0";

    // Couleurs de la charte graphique
    public static final Color COULEUR_PRIMAIRE = new Color(0x1A, 0x73, 0xE8); // Bleu Google
    public static final Color COULEUR_SECONDAIRE = new Color(0x34, 0xA8, 0x53); // Vert
    public static final Color COULEUR_DANGER = new Color(0xEA, 0x43, 0x35); // Rouge
    public static final Color COULEUR_AVERTISSEMENT = new Color(0xFB, 0xBC, 0x04); // Jaune

    public static final Color COULEUR_FOND_SIDEBAR = new Color(0x1E, 0x1E, 0x2E);
    public static final Color COULEUR_TEXTE_SIDEBAR = new Color(0xCD, 0xD6, 0xF4);
    public static final Color COULEUR_SELECTION_SIDEBAR = new Color(0x31, 0x31, 0x4A);
    public static final Color COULEUR_FOND_HEADER = new Color(0x18, 0x18, 0x2B);

    // Polices
    public static final Font FONT_TITRE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SOUS_TITRE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_NORMALE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MENU = new Font("Segoe UI", Font.PLAIN, 14);

    // Dimensions standard
    public static final Dimension TAILLE_BOUTON_PRINCIPAL = new Dimension(140, 36);
    public static final Dimension TAILLE_BOUTON = new Dimension(110, 32);
    public static final int MARGE = 12;

    // Rôles utilisateurs
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DIRECTEUR = "DIRECTEUR";
    public static final String ROLE_SECRETAIRE = "SECRETAIRE";

    // Sexe
    public static final String[] SEXES = { "M", "F" };
    public static final String[] SEXES_LIBELLE = { "Masculin", "Féminin" };

    // Lien parent
    public static final String[] LIENS_PARENT = { "Père", "Mère", "Tuteur", "Tutrice", "Grand-père", "Grand-mère",
            "Autre" };

    // Statut inscription
    public static final String STATUT_ACTIF = "ACTIF";
    public static final String STATUT_INACTIF = "INACTIF";
    public static final String STATUT_TRANSFERE = "TRANSFERE";

    private Constantes() {
    }
}
