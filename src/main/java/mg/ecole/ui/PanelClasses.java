package mg.ecole.ui;

import mg.ecole.dao.ClasseDAO;
import mg.ecole.dao.EnseignantDAO;
import mg.ecole.dao.NiveauDAO;
import mg.ecole.modele.Classe;
import mg.ecole.modele.Enseignant;
import mg.ecole.modele.Niveau;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de gestion des classes avec recherche multicritère.
 */
public class PanelClasses extends JPanel {

    private final MainFrame mainFrame;
    private final ClasseDAO classeDAO;
    private final NiveauDAO niveauDAO;
    private final EnseignantDAO enseignantDAO;

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField txtNom;
    private final JComboBox<String> cmbNiveau;
    private final JComboBox<String> cmbAnnee;

    private List<Classe> listeCourante;
    private List<Niveau> niveauxDisponibles;
    private List<String> anneesDisponibles;

    public PanelClasses(MainFrame mainFrame, ClasseDAO classeDAO,
            NiveauDAO niveauDAO, EnseignantDAO enseignantDAO) {
        this.mainFrame = mainFrame;
        this.classeDAO = classeDAO;
        this.niveauDAO = niveauDAO;
        this.enseignantDAO = enseignantDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));
        headerPanel.add(UIFactory.labelTitre("Gestion des Classes", "classes.svg"), BorderLayout.WEST);
        JButton btnAjouter = UIFactory.boutonPrincipal("Ajouter");
        btnAjouter.setIcon(UIFactory.icone("plus.svg", 16));
        btnAjouter.addActionListener(e -> dialogClasse(null));
        headerPanel.add(btnAjouter, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ── Barre de recherche multicritère ───────────────────────────────────
        JPanel barreRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barreRecherche.setOpaque(false);
        barreRecherche.setBorder(BorderFactory.createTitledBorder(" Recherche multicritère "));

        txtNom = UIFactory.champTexte(14);
        cmbNiveau = new JComboBox<>();
        cmbNiveau.addItem("Tous les niveaux");
        cmbAnnee = new JComboBox<>();
        cmbAnnee.addItem("Toutes les années");

        barreRecherche.add(UIFactory.label("Nom classe :"));
        barreRecherche.add(txtNom);
        barreRecherche.add(UIFactory.label("Niveau :"));
        barreRecherche.add(cmbNiveau);
        barreRecherche.add(UIFactory.label("Année scol. :"));
        barreRecherche.add(cmbAnnee);

        JButton btnRech = UIFactory.boutonPrincipal("Rechercher");
        btnRech.setIcon(UIFactory.icone("search.svg", 16));
        JButton btnReset = UIFactory.boutonSecondaire("");
        btnReset.setIcon(UIFactory.icone("refresh.svg", 16));
        btnRech.addActionListener(e -> rechercher());
        btnReset.addActionListener(e -> {
            txtNom.setText("");
            cmbNiveau.setSelectedIndex(0);
            cmbAnnee.setSelectedIndex(0);
            rafraichir();
        });
        barreRecherche.add(btnRech);
        barreRecherche.add(btnReset);

        // ── Tableau ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[] { "#", "Nom", "Niveau", "Année scol.", "Enseignant", "Élèves", "Capacité max" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centre = new JPanel(new BorderLayout(0, 8));
        centre.setOpaque(false);
        centre.add(barreRecherche, BorderLayout.NORTH);
        centre.add(scrollPane, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // ── Boutons bas ──────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.setOpaque(false);
        JButton btnVoirEleves = UIFactory.boutonSecondaire("Voir les élèves");
        btnVoirEleves.setIcon(UIFactory.icone("students.svg", 16));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.setIcon(UIFactory.icone("edit.svg", 16));
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.setIcon(UIFactory.icone("delete.svg", 16));
        btnVoirEleves.addActionListener(e -> voirElevesClasse());
        btnModifier.addActionListener(e -> dialogClasse(getClasseSelectionnee()));
        btnSupprimer.addActionListener(e -> supprimerSelectionnee());
        btnPanel.add(btnVoirEleves);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void rafraichir() {
        try {
            niveauxDisponibles = niveauDAO.listerTous();
            anneesDisponibles = classeDAO.listerAnneesScolaires();

            // Rafraîchir combo niveaux
            cmbNiveau.removeAllItems();
            cmbNiveau.addItem("Tous les niveaux");
            for (Niveau n : niveauxDisponibles)
                cmbNiveau.addItem(n.getCode() + " - " + n.getLibelle());

            // Rafraîchir combo années
            cmbAnnee.removeAllItems();
            cmbAnnee.addItem("Toutes les années");
            String anneeActuelle = mainFrame.getAnneeScolaireActuelle();
            if (!anneesDisponibles.contains(anneeActuelle))
                anneesDisponibles.add(0, anneeActuelle);
            for (String a : anneesDisponibles)
                cmbAnnee.addItem(a);

            // Sélectionner l'année courante par défaut
            cmbAnnee.setSelectedItem(anneeActuelle);

            listeCourante = classeDAO.listerParAnnee(anneeActuelle);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechercher() {
        String nom = txtNom.getText().trim();
        String niveauCode = null;
        if (cmbNiveau.getSelectedIndex() > 0 && niveauxDisponibles != null) {
            Niveau n = niveauxDisponibles.get(cmbNiveau.getSelectedIndex() - 1);
            niveauCode = n.getCode();
        }
        String annee = cmbAnnee.getSelectedIndex() > 0 ? (String) cmbAnnee.getSelectedItem() : null;
        try {
            listeCourante = classeDAO.listerAvecFiltre(nom.isEmpty() ? null : nom, niveauCode, annee);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Classe> classes) {
        tableModel.setRowCount(0);
        for (int i = 0; i < classes.size(); i++) {
            Classe c = classes.get(i);
            tableModel.addRow(new Object[] { i + 1, c.getNom(),
                    (c.getNiveauCode() != null ? c.getNiveauCode() + " - " + c.getNiveauLibelle() : ""),
                    c.getAnneeScolaire(),
                    (c.getEnseignantNom() != null ? c.getEnseignantNom() : "Non assigné"),
                    c.getNombreEleves(), c.getCapaciteMax() });
        }
    }

    private Classe getClasseSelectionnee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une classe.", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return listeCourante.get(table.convertRowIndexToModel(row));
    }

    private void voirElevesClasse() {
        Classe c = getClasseSelectionnee();
        if (c == null)
            return;
        // Naviguer vers le panel élèves filtré sur cette classe
        mainFrame.naviguerVers("ELEVES");
    }

    private void dialogClasse(Classe existante) {
        boolean isEdit = existante != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Modifier la classe" : "Ajouter une classe", true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 24, 8, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNomF = UIFactory.champTexte(20);
        JComboBox<String> cmbNiveauF = new JComboBox<>();
        JTextField txtAnneeF = UIFactory.champTexte(12);
        JComboBox<String> cmbEnseignantF = new JComboBox<>();
        JSpinner spnCap = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
        JTextField txtDescF = UIFactory.champTexte(22);

        // Peupler niveaux
        cmbNiveauF.addItem("-- Sélectionner --");
        if (niveauxDisponibles != null)
            for (Niveau n : niveauxDisponibles)
                cmbNiveauF.addItem(n.getCode() + " - " + n.getLibelle());

        // Peupler enseignants
        List<Enseignant> enseignants = new ArrayList<>();
        cmbEnseignantF.addItem("-- Aucun (non assigné) --");
        try {
            enseignants = enseignantDAO.listerTous();
            for (Enseignant e : enseignants)
                cmbEnseignantF.addItem(e.getMatricule() + " - " + e.getNomComplet());
        } catch (Exception ignore) {
        }

        txtAnneeF.setText(mainFrame.getAnneeScolaireActuelle());

        if (isEdit) {
            txtNomF.setText(existante.getNom());
            for (int i = 0; i < niveauxDisponibles.size(); i++) {
                if (niveauxDisponibles.get(i).getId() == existante.getNiveauId()) {
                    cmbNiveauF.setSelectedIndex(i + 1);
                    break;
                }
            }
            txtAnneeF.setText(existante.getAnneeScolaire());
            if (existante.getEnseignantId() != null) {
                for (int i = 0; i < enseignants.size(); i++) {
                    if (enseignants.get(i).getId() == existante.getEnseignantId()) {
                        cmbEnseignantF.setSelectedIndex(i + 1);
                        break;
                    }
                }
            }
            spnCap.setValue(existante.getCapaciteMax());
            txtDescF.setText(existante.getDescription());
        }

        int r = 0;
        ajouterLigne(form, gbc, r++, "Nom classe *", txtNomF);
        ajouterLigne(form, gbc, r++, "Niveau *", cmbNiveauF);
        ajouterLigne(form, gbc, r++, "Année scolaire *", txtAnneeF);
        ajouterLigne(form, gbc, r++, "Enseignant", cmbEnseignantF);
        ajouterLigne(form, gbc, r++, "Capacité max", spnCap);
        ajouterLigne(form, gbc, r, "Description", txtDescF);

        final List<Enseignant> enseignantsFinal = enseignants;
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = UIFactory.boutonPrincipal(isEdit ? "Enregistrer" : "Ajouter");
        JButton btnAnnul = UIFactory.boutonSecondaire("Annuler");
        btnAnnul.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(ev -> {
            String nom2 = txtNomF.getText().trim(), annee = txtAnneeF.getText().trim();
            if (nom2.isEmpty() || annee.isEmpty() || cmbNiveauF.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(dialog, "Nom, niveau et année scolaire sont obligatoires.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Niveau niveauChoisi = niveauxDisponibles.get(cmbNiveauF.getSelectedIndex() - 1);
            Integer enseignantId = null;
            if (cmbEnseignantF.getSelectedIndex() > 0)
                enseignantId = enseignantsFinal.get(cmbEnseignantF.getSelectedIndex() - 1).getId();
            try {
                Classe c = isEdit ? existante : new Classe();
                c.setNom(nom2);
                c.setNiveauId(niveauChoisi.getId());
                c.setAnneeScolaire(annee);
                c.setEnseignantId(enseignantId);
                c.setCapaciteMax((Integer) spnCap.getValue());
                c.setDescription(txtDescF.getText().trim());
                if (isEdit) {
                    classeDAO.modifier(c);
                    mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "MODIFICATION CLASSE",
                            "Mise à jour de la classe " + c.getNom());
                } else {
                    classeDAO.ajouter(c);
                    mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "NOUVELLE CLASSE",
                            "Création de la classe " + c.getNom());
                }
                rafraichir();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        bp.add(btnAnnul);
        bp.add(btnOk);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void supprimerSelectionnee() {
        Classe c = getClasseSelectionnee();
        if (c == null)
            return;
        int rep = JOptionPane.showConfirmDialog(this,
                "Supprimer la classe \"" + c.getNom() + "\" (" + c.getAnneeScolaire() + ") ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            try {
                classeDAO.supprimer(c.getId());
                mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "SUPPRESSION CLASSE",
                        "Suppression de la classe " + c.getNom());
                rafraichir();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ajouterLigne(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(UIFactory.label(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(comp, gbc);
    }
}
