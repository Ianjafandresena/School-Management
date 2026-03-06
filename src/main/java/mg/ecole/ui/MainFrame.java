package mg.ecole.ui;

import mg.ecole.dao.*;
import mg.ecole.database.DatabaseManager;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Fenêtre principale de l'application.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);

    private final PanelAccueil panelAccueil;
    private final PanelEleves panelEleves;
    private final PanelClasses panelClasses;
    private final PanelNiveaux panelNiveaux;
    private final PanelEnseignants panelEnseignants;
    private final PanelPaiements panelPaiements;
    private final PanelComptabilite panelComptabilite;
    private final PanelStatistiques panelStatistiques;
    private final PanelFraisScolaires panelFraisScolaires;
    private final PanelParametres panelParametres;
    private final PanelJournal panelJournal;

    private final EleveDAO eleveDAO = new EleveDAO();
    private final ClasseDAO classeDAO = new ClasseDAO();
    private final EnseignantDAO enseignantDAO = new EnseignantDAO();
    private final NiveauDAO niveauDAO = new NiveauDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final FraisScolaireDAO fraisDAO = new FraisScolaireDAO();
    private final StatistiquesDAO statsDAO = new StatistiquesDAO();
    private final AnneeScolaireDAO anneeDAO = new AnneeScolaireDAO();
    private final JournalDAO journalDAO = new JournalDAO();

    private final ButtonGroup sidebarGroup = new ButtonGroup();
    private JLabel lblUser;

    private String currentUser = "admin";
    // Supprimé currentRole car inutilisé

    public MainFrame() {
        setTitle(Constantes.NOM_APP + " v" + Constantes.VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setPreferredSize(new Dimension(1280, 800));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception ignore) {
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int rep = JOptionPane.showConfirmDialog(MainFrame.this, "Quitter l'application ?", "Quitter",
                        JOptionPane.YES_NO_OPTION);
                if (rep == JOptionPane.YES_OPTION) {
                    journalDAO.log(currentUser, "DECONNEXION", "Fermeture de l'application");
                    DatabaseManager.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });

        panelAccueil = new PanelAccueil(this, eleveDAO, classeDAO, enseignantDAO);
        panelEleves = new PanelEleves(this, eleveDAO, classeDAO);
        panelClasses = new PanelClasses(this, classeDAO, niveauDAO, enseignantDAO);
        panelNiveaux = new PanelNiveaux(this, niveauDAO);
        panelEnseignants = new PanelEnseignants(this, enseignantDAO);
        panelPaiements = new PanelPaiements(this, paiementDAO, eleveDAO, classeDAO);
        panelComptabilite = new PanelComptabilite(this, paiementDAO, classeDAO, niveauDAO);
        panelStatistiques = new PanelStatistiques(this, statsDAO);
        panelFraisScolaires = new PanelFraisScolaires(this, fraisDAO, niveauDAO);
        panelParametres = new PanelParametres(this, anneeDAO);
        panelJournal = new PanelJournal(journalDAO);

        contentArea.add(panelAccueil, "ACCUEIL");
        contentArea.add(panelEleves, "ELEVES");
        contentArea.add(panelClasses, "CLASSES");
        contentArea.add(panelNiveaux, "NIVEAUX");
        contentArea.add(panelEnseignants, "ENSEIGNANTS");
        contentArea.add(panelPaiements, "PAIEMENTS");
        contentArea.add(panelComptabilite, "COMPTA");
        contentArea.add(panelStatistiques, "STATS");
        contentArea.add(panelFraisScolaires, "FRAIS");
        contentArea.add(panelParametres, "PARAMS");
        contentArea.add(panelJournal, "JOURNAL");

        setLayout(new BorderLayout());
        add(creerSidebar(), BorderLayout.WEST);
        add(creerHeader(), BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static FlatSVGIcon icone(String relativePath, int size) {
        FlatSVGIcon icon = new FlatSVGIcon("icons/" + relativePath);
        return icon.derive(size, size);
    }

    public void setUserSession(String login, String role) {
        this.currentUser = login;
        if (lblUser != null)
            lblUser.setText(login + " (" + role + ")");
        journalDAO.log(login, "CONNEXION", "Utilisateur connecté");
    }

    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Constantes.COULEUR_FOND_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Logo
        try {
            ImageIcon logoIcon = UIFactory.iconeImage("wavolution.png", 64);
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoLabel.setBorder(new EmptyBorder(25, 0, 10, 0));
            sidebar.add(logoLabel);
        } catch (Exception e) {
            JLabel logoLabel = new JLabel("📚 ECOLE", SwingConstants.CENTER);
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoLabel.setBorder(new EmptyBorder(25, 0, 10, 0));
            sidebar.add(logoLabel);
        }

        JLabel titleLabel = new JLabel("Menu Principal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(0x7F, 0x84, 0xBE));
        titleLabel.setBorder(new EmptyBorder(5, 20, 15, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);

        sidebar.add(creerSeparateur());
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(creerBoutonNav("Accueil", "ACCUEIL", "home.svg"));
        sidebar.add(creerBoutonNav("Élèves", "ELEVES", "students.svg"));
        sidebar.add(creerBoutonNav("Classes", "CLASSES", "classes.svg"));
        sidebar.add(creerBoutonNav("Niveaux", "NIVEAUX", "levels.svg"));
        sidebar.add(creerBoutonNav("Enseignants", "ENSEIGNANTS", "teachers.svg"));

        sidebar.add(creerSeparateur());
        sidebar.add(creerSectionLabel("FINANCES"));
        sidebar.add(creerBoutonNav("Paiements", "PAIEMENTS", "payments.svg"));
        sidebar.add(creerBoutonNav("Comptabilité", "COMPTA", "accounting.svg"));

        sidebar.add(creerSeparateur());
        sidebar.add(creerSectionLabel("ANALYSE & CONFIG"));
        sidebar.add(creerBoutonNav("Statistiques", "STATS", "stats.svg"));
        sidebar.add(creerBoutonNav("Frais Scolaires", "FRAIS", "settings.svg"));
        sidebar.add(creerBoutonNav("Journal", "JOURNAL", "journal.svg"));
        sidebar.add(creerBoutonNav("Paramètres", "PARAMS", "settings.svg"));

        sidebar.add(Box.createVerticalGlue());

        lblUser = new JLabel("👤 Connecté...");
        lblUser.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblUser.setForeground(new Color(0xA0, 0xAA, 0xC0));
        lblUser.setBorder(new EmptyBorder(10, 20, 10, 20));
        sidebar.add(lblUser);

        sidebar.add(lblUser);

        sidebar.add(creerSeparateur());
        JButton btnQuit = new JButton("Quitter");
        btnQuit.setIcon(UIFactory.icone("exit.svg", 18));
        btnQuit.setIconTextGap(12);
        btnQuit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnQuit.setForeground(new Color(0xF3, 0x8B, 0xA8));
        btnQuit.setBackground(Constantes.COULEUR_FOND_SIDEBAR);
        btnQuit.setFocusPainted(false);
        btnQuit.setContentAreaFilled(false);
        btnQuit.setBorderPainted(false);
        btnQuit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQuit.setHorizontalAlignment(SwingConstants.LEFT);
        btnQuit.setBorder(new EmptyBorder(14, 20, 14, 12));
        btnQuit.setMaximumSize(new Dimension(240, 50));
        btnQuit.addActionListener(
                e -> dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        sidebar.add(btnQuit);
        sidebar.add(Box.createVerticalStrut(8));

        return sidebar;
    }

    private JLabel creerSectionLabel(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(0x58, 0x5B, 0x70));
        l.setBorder(new EmptyBorder(10, 20, 5, 20));
        return l;
    }

    private JToggleButton creerBoutonNav(String texte, String cle, String iconPath) {
        JToggleButton btn = new JToggleButton(texte) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(Constantes.COULEUR_SELECTION_SIDEBAR);
                    g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
                    g2.setColor(Constantes.COULEUR_PRIMAIRE);
                    g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setIcon(UIFactory.icone(iconPath, 20));
        btn.setIconTextGap(12);
        btn.setFont(Constantes.FONT_MENU);
        btn.setForeground(Constantes.COULEUR_TEXTE_SIDEBAR);
        btn.setBackground(Constantes.COULEUR_FOND_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 12));
        btn.setMaximumSize(new Dimension(240, 50));
        btn.addActionListener(e -> naviguerVers(cle));
        sidebarGroup.add(btn);
        if ("ACCUEIL".equals(cle))
            btn.setSelected(true);
        return btn;
    }

    private JSeparator creerSeparateur() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(0x31, 0x31, 0x4A));
        s.setMaximumSize(new Dimension(220, 1));
        return s;
    }

    private JPanel creerHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Constantes.COULEUR_FOND_HEADER);
        h.setPreferredSize(new Dimension(0, 56));
        h.setBorder(new EmptyBorder(8, 20, 8, 20));
        JLabel t = new JLabel(Constantes.NOM_APP);
        t.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t.setForeground(Color.WHITE);
        JLabel a = new JLabel("Année : " + getAnneeScolaireActuelle());
        a.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        a.setForeground(new Color(0xA0, 0xAA, 0xC0));
        h.add(t, BorderLayout.WEST);
        h.add(a, BorderLayout.EAST);
        return h;
    }

    public void naviguerVers(String cle) {
        cardLayout.show(contentArea, cle);
        switch (cle) {
            case "ACCUEIL":
                panelAccueil.rafraichir();
                break;
            case "ELEVES":
                panelEleves.rafraichir();
                break;
            case "CLASSES":
                panelClasses.rafraichir();
                break;
            case "NIVEAUX":
                panelNiveaux.rafraichir();
                break;
            case "ENSEIGNANTS":
                panelEnseignants.rafraichir();
                break;
            case "PAIEMENTS":
                panelPaiements.rafraichir();
                break;
            case "COMPTA":
                panelComptabilite.rafraichir();
                break;
            case "STATS":
                panelStatistiques.rafraichir();
                break;
            case "FRAIS":
                panelFraisScolaires.rafraichir();
                break;
            case "JOURNAL":
                panelJournal.rafraichir();
                break;
            case "PARAMS":
                panelParametres.rafraichir();
                break;
        }
    }

    public String getAnneeScolaireActuelle() {
        try {
            java.sql.ResultSet rs = DatabaseManager.getInstance().getConnection().createStatement()
                    .executeQuery("SELECT valeur FROM parametres WHERE cle='annee_scolaire'");
            if (rs.next())
                return rs.getString(1);
        } catch (Exception ignored) {
        }
        return "2025-2026";
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public JournalDAO getJournalDAO() {
        return journalDAO;
    }

    public String getNomEcole() {
        try {
            return anneeDAO.getParametre("nom_ecole", "Ecole Primaire");
        } catch (Exception e) {
            return "Ecole";
        }
    }

    public String getDevise() {
        try {
            return anneeDAO.getParametre("devise", "Ar");
        } catch (Exception e) {
            return "Ar";
        }
    }

    public FraisScolaireDAO getFraisDAO() {
        return fraisDAO;
    }

    public NiveauDAO getNiveauDAO() {
        return niveauDAO;
    }
}
