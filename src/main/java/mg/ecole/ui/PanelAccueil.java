package mg.ecole.ui;

import mg.ecole.dao.ClasseDAO;
import mg.ecole.dao.EleveDAO;
import mg.ecole.dao.EnseignantDAO;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Panneau d'accueil : tableau de bord avec statistiques.
 */
public class PanelAccueil extends JPanel {

    private final MainFrame mainFrame;
    private final EleveDAO eleveDAO;
    private final ClasseDAO classeDAO;
    private final EnseignantDAO enseignantDAO;

    private JLabel lblNbEleves;
    private JLabel lblNbClasses;
    private JLabel lblNbEnseignants;
    private JLabel lblAnneeInfo;

    public PanelAccueil(MainFrame mainFrame, EleveDAO eleveDAO,
            ClasseDAO classeDAO, EnseignantDAO enseignantDAO) {
        this.mainFrame = mainFrame;
        this.eleveDAO = eleveDAO;
        this.classeDAO = classeDAO;
        this.enseignantDAO = enseignantDAO;
        construire();
    }

    private void construire() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Titre
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitre = UIFactory.labelTitre("Tableau de bord", "home.svg");
        lblAnneeInfo = new JLabel();
        lblAnneeInfo.setFont(Constantes.FONT_NORMALE);
        lblAnneeInfo.setForeground(new Color(0x70, 0x80, 0x90));
        headerPanel.add(lblTitre, BorderLayout.WEST);
        headerPanel.add(lblAnneeInfo, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ── Cartes statistiques ──────────────────────────────────────────────
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(24, 0, 24, 0));

        lblNbEleves = new JLabel("…");
        lblNbClasses = new JLabel("…");
        lblNbEnseignants = new JLabel("…");

        cardsPanel
                .add(creerCarte("Élèves inscrits", "students.svg", lblNbEleves, Constantes.COULEUR_PRIMAIRE, "ELEVES"));
        cardsPanel.add(
                creerCarte("Classes actives", "classes.svg", lblNbClasses, Constantes.COULEUR_SECONDAIRE, "CLASSES"));
        cardsPanel.add(creerCarte("Enseignants", "teachers.svg", lblNbEnseignants, new Color(0x8A, 0x2B, 0xE2),
                "ENSEIGNANTS"));

        add(cardsPanel, BorderLayout.CENTER);

        // ── Accès rapides ────────────────────────────────────────────────────
        JPanel raccourcisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        raccourcisPanel.setOpaque(false);
        raccourcisPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                " Accès rapides "));

        JButton btnAjouterEleve = UIFactory.boutonPrincipal("Nouvel élève");
        btnAjouterEleve.setIcon(UIFactory.icone("plus.svg", 20));
        btnAjouterEleve.setPreferredSize(new Dimension(180, 45));
        btnAjouterEleve.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAjouterEleve.addActionListener(e -> mainFrame.naviguerVers("ELEVES"));

        JButton btnAjouterClasse = UIFactory.boutonSucces("Nouvelle classe");
        btnAjouterClasse.setIcon(UIFactory.icone("plus.svg", 20));
        btnAjouterClasse.setPreferredSize(new Dimension(180, 45));
        btnAjouterClasse.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAjouterClasse.addActionListener(e -> mainFrame.naviguerVers("CLASSES"));

        JButton btnAjouterEnseignant = UIFactory.boutonSecondaire("Nouvel enseignant");
        btnAjouterEnseignant.setIcon(UIFactory.icone("plus.svg", 20));
        btnAjouterEnseignant.setPreferredSize(new Dimension(180, 45));
        btnAjouterEnseignant.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAjouterEnseignant.addActionListener(e -> mainFrame.naviguerVers("ENSEIGNANTS"));

        raccourcisPanel.add(btnAjouterEleve);
        raccourcisPanel.add(btnAjouterClasse);
        raccourcisPanel.add(btnAjouterEnseignant);

        add(raccourcisPanel, BorderLayout.SOUTH);
    }

    private JPanel creerCarte(String titre, String iconName, JLabel lblValeur, Color couleur, String cible) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Bordure colorée gauche
                g2.setColor(couleur);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        card.setBackground(UIManager.getColor("Panel.background") != null
                ? UIManager.getColor("Panel.background")
                : new Color(0xF5, 0xF5, 0xF5));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 18, 12, 18));
        card.setPreferredSize(new Dimension(240, 130)); // Légèrement plus grande
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mainFrame.naviguerVers(cible);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(0xE8, 0xF0, 0xFE));
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UIManager.getColor("Panel.background") != null
                        ? UIManager.getColor("Panel.background")
                        : new Color(0xF5, 0xF5, 0xF5));
                card.repaint();
            }
        });

        // Icone + Titre en haut
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JLabel lblIcon = new JLabel(UIFactory.icone(iconName, 24));
        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(Constantes.FONT_BOLD);
        top.add(lblIcon);
        top.add(lblTitre);

        lblValeur.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblValeur.setForeground(couleur);
        lblValeur.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(lblValeur, BorderLayout.CENTER);
        return card;
    }

    /** Rafraîchit les statistiques depuis la base de données. */
    public void rafraichir() {
        String annee = mainFrame.getAnneeScolaireActuelle();
        lblAnneeInfo.setText("Année scolaire " + annee);
        SwingUtilities.invokeLater(() -> {
            try {
                int nbEleves = eleveDAO.compterEleves(annee);
                int nbClasses = classeDAO.listerParAnnee(annee).size();
                int nbEnseignants = enseignantDAO.listerTous().size();
                lblNbEleves.setText(String.valueOf(nbEleves));
                lblNbClasses.setText(String.valueOf(nbClasses));
                lblNbEnseignants.setText(String.valueOf(nbEnseignants));
            } catch (SQLException e) {
                lblNbEleves.setText("Err.");
            }
        });
    }
}
