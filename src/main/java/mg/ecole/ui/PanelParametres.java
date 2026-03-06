package mg.ecole.ui;

import mg.ecole.dao.AnneeScolaireDAO;
import mg.ecole.modele.AnneeScolaire;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel de configuration des paramètres de l'école et des années scolaires.
 */
public class PanelParametres extends JPanel {

    private final AnneeScolaireDAO anneeDAO;

    // Champs école
    private final JTextField txtNomEcole;
    private final JTextField txtDevise;

    // Années scolaires
    private final DefaultListModel<AnneeScolaire> listModel;
    private final JList<AnneeScolaire> listAnnees;

    public PanelParametres(MainFrame mainFrame, AnneeScolaireDAO anneeDAO) {
        this.anneeDAO = anneeDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 22, 16, 22));

        // En-tête
        add(UIFactory.labelTitre("⚙ Paramètres Système"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 0, 0, 0));

        // ── Gauche : Paramètres Généraux ──
        JPanel pnlGauche = new JPanel(new GridBagLayout());
        pnlGauche.setOpaque(false);
        pnlGauche.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Paramètres Généraux "),
                new EmptyBorder(20, 20, 20, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNomEcole = UIFactory.champTexte(20);
        txtDevise = UIFactory.champTexte(6);

        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlGauche.add(UIFactory.label("Nom de l'école :"), gbc);
        gbc.gridx = 1;
        pnlGauche.add(txtNomEcole, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlGauche.add(UIFactory.label("Devise (Ar, €, $) :"), gbc);
        gbc.gridx = 1;
        pnlGauche.add(txtDevise, gbc);

        JButton btnSaveParam = UIFactory.boutonPrincipal("Enregistrer");
        btnSaveParam.addActionListener(e -> enregistrerParametres());
        gbc.gridx = 1;
        gbc.gridy = 2;
        pnlGauche.add(btnSaveParam, gbc);

        // ── Droite : Gestion des Années ──
        JPanel pnlDroite = new JPanel(new BorderLayout(0, 10));
        pnlDroite.setOpaque(false);
        pnlDroite.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Gestion des Années "),
                new EmptyBorder(15, 15, 15, 15)));

        listModel = new DefaultListModel<>();
        listAnnees = new JList<>(listModel);
        listAnnees.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                AnneeScolaire a = (AnneeScolaire) value;
                if (a.isActive()) {
                    l.setText("⭐ " + a.getLibelle() + " (Active)");
                    l.setForeground(new Color(0x3B, 0x82, 0xF6));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else {
                    l.setText("   " + a.getLibelle());
                }
                return l;
            }
        });

        pnlDroite.add(new JScrollPane(listAnnees), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnAjoutAnnee = UIFactory.boutonPrincipal("+ Nouvelle");
        JButton btnActiveAnnee = UIFactory.boutonSucces("Activer");
        btnAjoutAnnee.addActionListener(e -> ajouterAnnee());
        btnActiveAnnee.addActionListener(e -> activerAnnee());
        btnPanel.add(btnActiveAnnee);
        btnPanel.add(btnAjoutAnnee);
        pnlDroite.add(btnPanel, BorderLayout.SOUTH);

        content.add(pnlGauche);
        content.add(pnlDroite);
        add(content, BorderLayout.CENTER);
    }

    public void rafraichir() {
        try {
            txtNomEcole.setText(anneeDAO.getParametre("nom_ecole", "Ecole Primaire"));
            txtDevise.setText(anneeDAO.getParametre("devise", "Ar"));

            listModel.clear();
            List<AnneeScolaire> annees = anneeDAO.lister();
            for (AnneeScolaire a : annees)
                listModel.addElement(a);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur parametres : " + e.getMessage());
        }
    }

    private void enregistrerParametres() {
        try {
            anneeDAO.majParametre("nom_ecole", txtNomEcole.getText().trim());
            anneeDAO.majParametre("devise", txtDevise.getText().trim());
            JOptionPane.showMessageDialog(this, "Paramètres mis à jour !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }

    private void activerAnnee() {
        AnneeScolaire selected = listAnnees.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une année dans la liste.");
            return;
        }
        try {
            anneeDAO.activer(selected.getId());
            rafraichir();
            JOptionPane.showMessageDialog(this, "L'année " + selected.getLibelle() + " est maintenant l'année active.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }

    private void ajouterAnnee() {
        String libelle = JOptionPane.showInputDialog(this, "Saisissez le libellé de l'année (ex: 2026-2027) :");
        if (libelle != null && !libelle.trim().isEmpty()) {
            AnneeScolaire a = new AnneeScolaire();
            a.setLibelle(libelle.trim());
            a.setActive(false);
            try {
                anneeDAO.ajouter(a);
                rafraichir();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
            }
        }
    }
}
