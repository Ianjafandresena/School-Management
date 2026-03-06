package mg.ecole.ui;

import mg.ecole.dao.ClasseDAO;
import mg.ecole.dao.EleveDAO;
import mg.ecole.modele.Classe;
import mg.ecole.modele.Eleve;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import com.github.lgooddatepicker.components.DatePicker;
import java.util.List;

/**
 * Panel de gestion des élèves.
 * Recherche multicritère : nom, sexe, classe, année scolaire.
 * CRUD complet avec support multi-parents (Père, Mère, Tuteur).
 */
public class PanelEleves extends JPanel {

    private final MainFrame mainFrame;
    private final EleveDAO eleveDAO;
    private final ClasseDAO classeDAO;

    private final DefaultTableModel tableModel;
    private final JTable table;

    // Filtres de recherche
    private final JTextField txtNom;
    private final JComboBox<String> cmbSexe;
    private final JComboBox<String> cmbClasse;
    private final JComboBox<String> cmbAnnee;

    private List<Eleve> listeCourante;
    private List<Classe> classesDisponibles;
    private List<String> anneesDisponibles;

    private JLabel lblCompteur;

    public PanelEleves(MainFrame mainFrame, EleveDAO eleveDAO, ClasseDAO classeDAO) {
        this.mainFrame = mainFrame;
        this.eleveDAO = eleveDAO;
        this.classeDAO = classeDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));
        headerPanel.add(UIFactory.labelTitre("Gestion des Élèves", "eleve.svg"), BorderLayout.WEST);
        JPanel btnHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnHeader.setOpaque(false);
        JButton btnAjouter = UIFactory.boutonPrincipal("Ajouter");
        btnAjouter.setIcon(UIFactory.icone("plus.svg", 16));
        JButton btnImporter = UIFactory.boutonSecondaire("Importer CSV");
        btnImporter.setIcon(UIFactory.icone("import.svg", 16));
        btnAjouter.addActionListener(e -> dialogEleve(null));
        btnImporter.addActionListener(e -> importerCSV());
        btnHeader.add(btnImporter);
        btnHeader.add(btnAjouter);
        headerPanel.add(btnHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ── Barre de recherche multicritère ───────────────────────────────────
        JPanel barreRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barreRecherche.setOpaque(false);
        barreRecherche.setBorder(BorderFactory.createTitledBorder(" Recherche multicritère "));

        txtNom = UIFactory.champTexte(16);
        cmbSexe = new JComboBox<>(new String[] { "Tous", "M", "F" });
        cmbClasse = new JComboBox<>();
        cmbAnnee = new JComboBox<>();

        barreRecherche.add(UIFactory.label("Nom/Prénom :"));
        barreRecherche.add(txtNom);
        barreRecherche.add(UIFactory.label("Sexe :"));
        barreRecherche.add(cmbSexe);
        barreRecherche.add(UIFactory.label("Classe :"));
        barreRecherche.add(cmbClasse);
        barreRecherche.add(UIFactory.label("Année scol. :"));
        barreRecherche.add(cmbAnnee);

        JButton btnRech = UIFactory.boutonPrincipal("Rechercher");
        btnRech.setIcon(UIFactory.icone("search.svg", 16));
        JButton btnReset = UIFactory.boutonSecondaire("");
        btnReset.setIcon(UIFactory.icone("refresh.svg", 16));
        btnRech.addActionListener(e -> rechercher());
        btnReset.addActionListener(e -> {
            txtNom.setText("");
            cmbSexe.setSelectedIndex(0);
            cmbClasse.setSelectedIndex(0);
            cmbAnnee.setSelectedIndex(0);
            rafraichir();
        });
        barreRecherche.add(btnRech);
        barreRecherche.add(btnReset);

        // ── Tableau ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[] { "#", "Matricule", "Nom", "Prénom", "Sexe", "Classe", "Père", "Mère", "Contact" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(4).setPreferredWidth(45);

        JScrollPane scrollPane = new JScrollPane(table);

        lblCompteur = new JLabel("0 élève(s) trouvé(s)");
        lblCompteur.setFont(Constantes.FONT_SMALL);
        lblCompteur.setForeground(Color.GRAY);

        JPanel centre = new JPanel(new BorderLayout(0, 6));
        centre.setOpaque(false);
        centre.add(barreRecherche, BorderLayout.NORTH);
        centre.add(scrollPane, BorderLayout.CENTER);
        centre.add(lblCompteur, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        // ── Boutons bas ──────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.setOpaque(false);
        JButton btnFiche = UIFactory.boutonSecondaire("Fiche");
        btnFiche.setIcon(UIFactory.icone("fiche.svg", 16));
        JButton btnAffecter = UIFactory.boutonSecondaire("Affecter classe");
        btnAffecter.setIcon(UIFactory.icone("classe.svg", 16));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.setIcon(UIFactory.icone("edit.svg", 16));
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.setIcon(UIFactory.icone("delete.svg", 16));

        btnFiche.addActionListener(e -> afficherFiche());
        btnAffecter.addActionListener(e -> dialogAffecterClasse());
        btnModifier.addActionListener(e -> dialogEleve(getEleveSelectionne()));
        btnSupprimer.addActionListener(e -> supprimerSelectionne());

        btnPanel.add(btnFiche);
        btnPanel.add(btnAffecter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void rafraichir() {
        try {
            String annee = mainFrame.getAnneeScolaireActuelle();
            classesDisponibles = classeDAO.listerParAnnee(annee);
            anneesDisponibles = classeDAO.listerAnneesScolaires();

            cmbClasse.removeAllItems();
            cmbClasse.addItem("Toutes les classes");
            for (Classe c : classesDisponibles)
                cmbClasse.addItem(c.getNom());

            cmbAnnee.removeAllItems();
            cmbAnnee.addItem("Toutes les années");
            if (!anneesDisponibles.contains(annee))
                anneesDisponibles.add(0, annee);
            for (String a : anneesDisponibles)
                cmbAnnee.addItem(a);
            cmbAnnee.setSelectedItem(annee);

            listeCourante = eleveDAO.listerAvecFiltre(null, null, null, annee);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechercher() {
        String nom = txtNom.getText().trim();
        String sexe = cmbSexe.getSelectedIndex() == 0 ? null : (String) cmbSexe.getSelectedItem();
        Integer classeId = null;
        if (cmbClasse.getSelectedIndex() > 0 && classesDisponibles != null)
            classeId = classesDisponibles.get(cmbClasse.getSelectedIndex() - 1).getId();
        String annee = cmbAnnee.getSelectedIndex() > 0 ? (String) cmbAnnee.getSelectedItem() : null;
        try {
            listeCourante = eleveDAO.listerAvecFiltre(nom.isEmpty() ? null : nom, sexe, classeId, annee);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Eleve> eleves) {
        tableModel.setRowCount(0);
        for (int i = 0; i < eleves.size(); i++) {
            Eleve e = eleves.get(i);
            String t1 = e.getTelPere() != null ? e.getTelPere() : "";
            String t2 = e.getTelMere() != null ? e.getTelMere() : "";
            String contact = "";
            if (!t1.isEmpty() && !t2.isEmpty()) {
                contact = t1 + " / " + t2;
            } else if (!t1.isEmpty()) {
                contact = t1;
            } else {
                contact = t2;
            }
            tableModel.addRow(new Object[] {
                    i + 1, e.getMatricule(), e.getNom(), e.getPrenom(), e.getSexe(),
                    (e.getClasseNom() != null ? e.getClasseNom() : "Non inscrit"),
                    (e.getNomPere() != null ? e.getNomPere() : ""),
                    (e.getNomMere() != null ? e.getNomMere() : ""),
                    contact
            });
        }
        lblCompteur.setText(eleves.size() + " élève(s) trouvé(s)");
    }

    private Eleve getEleveSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un élève.", "Avertissement", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return listeCourante.get(table.convertRowIndexToModel(row));
    }

    private void dialogEleve(Eleve existant) {
        boolean isEdit = existant != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Modifier l'élève" : "Ajouter un élève", true);
        dialog.setSize(620, 680);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        // ─ Tab 1 : Infos personnelles ─────────────────────────────────────────
        JPanel tab1 = new JPanel(new GridBagLayout());
        tab1.setBorder(new EmptyBorder(14, 22, 8, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtMatricule = UIFactory.champTexte(14);
        JTextField txtNomF = UIFactory.champTexte(20);
        JTextField txtPrenomF = UIFactory.champTexte(20);
        JComboBox<String> cmbSexeF = new JComboBox<>(new String[] { "M", "F" });
        DatePicker dpNaissance = UIFactory.datePicker();
        JTextField txtLieuNaissF = UIFactory.champTexte(20);
        JTextField txtAdresseF = UIFactory.champTexte(22);

        if (isEdit) {
            txtMatricule.setText(existant.getMatricule());
            txtNomF.setText(existant.getNom());
            txtPrenomF.setText(existant.getPrenom());
            cmbSexeF.setSelectedItem(existant.getSexe());
            if (existant.getDateNaissance() != null && !existant.getDateNaissance().isEmpty()) {
                try {
                    dpNaissance.setDate(LocalDate.parse(existant.getDateNaissance()));
                } catch (Exception ignore) {
                }
            }
            txtLieuNaissF.setText(existant.getLieuNaissance());
            txtAdresseF.setText(existant.getAdresse());
        } else {
            try {
                txtMatricule.setText(eleveDAO.genererMatricule(mainFrame.getAnneeScolaireActuelle()));
            } catch (Exception ignore) {
            }
        }

        int r = 0;
        ajouterLigne(tab1, gbc, r++, "Matricule *", txtMatricule);
        ajouterLigne(tab1, gbc, r++, "Nom *", txtNomF);
        ajouterLigne(tab1, gbc, r++, "Prénom *", txtPrenomF);
        ajouterLigne(tab1, gbc, r++, "Sexe *", cmbSexeF);
        ajouterLigne(tab1, gbc, r++, "Date de naissance", dpNaissance);
        ajouterLigne(tab1, gbc, r++, "Lieu de naissance", txtLieuNaissF);
        ajouterLigne(tab1, gbc, r++, "Adresse", txtAdresseF);

        // ─ Tab 2 : Parents / Tuteurs ─────────────────────────────────────────
        JPanel tab2 = new JPanel(new GridBagLayout());
        tab2.setBorder(new EmptyBorder(14, 22, 8, 22));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(6, 6, 6, 6);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNomPere = UIFactory.champTexte(20);
        JTextField txtTelPere = UIFactory.champTexte(16);
        JTextField txtNomMere = UIFactory.champTexte(20);
        JTextField txtTelMere = UIFactory.champTexte(16);
        JTextField txtNomTuteur = UIFactory.champTexte(20);
        JTextField txtTelTuteur = UIFactory.champTexte(16);
        JComboBox<String> cmbTypeTuteur = new JComboBox<>(new String[] { "Père", "Mère", "Tuteur", "Tutrice" });

        if (isEdit) {
            txtNomPere.setText(existant.getNomPere());
            txtTelPere.setText(existant.getTelPere());
            txtNomMere.setText(existant.getNomMere());
            txtTelMere.setText(existant.getTelMere());
            txtNomTuteur.setText(existant.getNomTuteur());
            txtTelTuteur.setText(existant.getTelTuteur());
            cmbTypeTuteur.setSelectedItem(existant.getTypeTuteur());
        }

        r = 0;
        gbc2.gridwidth = 2;
        gbc2.insets = new Insets(15, 6, 8, 6); // Plus d'espace au dessus
        JLabel lblPere = UIFactory.label("— Informations du Père —");
        lblPere.setForeground(Constantes.COULEUR_PRIMAIRE);
        gbc2.gridy = r++;
        tab2.add(lblPere, gbc2);

        gbc2.gridwidth = 1;
        gbc2.insets = new Insets(6, 6, 6, 6); // Reset insets
        ajouterLigne(tab2, gbc2, r++, "Nom du Père", txtNomPere);
        ajouterLigne(tab2, gbc2, r++, "Téléphone Père", txtTelPere);

        gbc2.gridwidth = 2;
        gbc2.gridy = r++;
        gbc2.insets = new Insets(18, 6, 8, 6); // Espace avant Mère
        JLabel lblMere = UIFactory.label("— Informations de la Mère —");
        lblMere.setForeground(Constantes.COULEUR_PRIMAIRE);
        tab2.add(lblMere, gbc2);

        gbc2.gridwidth = 1;
        gbc2.insets = new Insets(6, 6, 6, 6);
        ajouterLigne(tab2, gbc2, r++, "Nom de la Mère", txtNomMere);
        ajouterLigne(tab2, gbc2, r++, "Téléphone Mère", txtTelMere);

        gbc2.gridwidth = 2;
        gbc2.gridy = r++;
        gbc2.insets = new Insets(18, 6, 8, 6); // Espace avant Tuteur
        JLabel lblTuteur = UIFactory.label("— Tuteur / Tutrice (si différent) —");
        lblTuteur.setForeground(Constantes.COULEUR_PRIMAIRE);
        tab2.add(lblTuteur, gbc2);

        gbc2.gridwidth = 1;
        gbc2.insets = new Insets(6, 6, 6, 6);
        ajouterLigne(tab2, gbc2, r++, "Nom Tuteur", txtNomTuteur);
        ajouterLigne(tab2, gbc2, r++, "Téléphone Tuteur", txtTelTuteur);
        ajouterLigne(tab2, gbc2, r++, "Tuteur principal", cmbTypeTuteur);

        // ─ Tab 3 : Inscription ──────────────────────────────────────────────
        JPanel tab3 = new JPanel(new GridBagLayout());
        tab3.setBorder(new EmptyBorder(14, 22, 8, 22));
        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = new Insets(10, 8, 10, 8);
        gbc3.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbClasseAdd = new JComboBox<>();
        if (classesDisponibles != null)
            for (Classe c : classesDisponibles)
                cmbClasseAdd.addItem(c.getNom());

        if (!isEdit) {
            ajouterLigne(tab3, gbc3, 0, "Affecter à la classe :", cmbClasseAdd);
        } else {
            tab3.add(UIFactory.label("Modification de classe via le bouton 'Affecter' uniquement."));
        }

        tabs.addTab("Infos personnelles", tab1);
        tabs.addTab("Parents / Tuteurs", tab2);
        if (!isEdit)
            tabs.addTab("Inscription", tab3);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = UIFactory.boutonPrincipal(isEdit ? "Enregistrer" : "Ajouter");
        JButton btnAnnul = UIFactory.boutonSecondaire("Annuler");
        btnAnnul.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(ev -> {
            String mat = txtMatricule.getText().trim(), nom = txtNomF.getText().trim(),
                    prenom = txtPrenomF.getText().trim();
            if (mat.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Matricule, Nom et Prénom sont obligatoires.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (eleveDAO.matriculeExiste(mat, isEdit ? existant.getId() : -1)) {
                    JOptionPane.showMessageDialog(dialog, "Ce matricule existe déjà.", "Doublon",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Eleve el = isEdit ? existant : new Eleve();
                el.setMatricule(mat);
                el.setNom(nom.toUpperCase());
                el.setPrenom(prenom);
                el.setSexe((String) cmbSexeF.getSelectedItem());
                el.setDateNaissance(dpNaissance.getDateStringOrEmptyString());
                el.setLieuNaissance(txtLieuNaissF.getText().trim());
                el.setAdresse(txtAdresseF.getText().trim());

                el.setNomPere(txtNomPere.getText().trim());
                el.setTelPere(txtTelPere.getText().trim());
                el.setNomMere(txtNomMere.getText().trim());
                el.setTelMere(txtTelMere.getText().trim());
                el.setNomTuteur(txtNomTuteur.getText().trim());
                el.setTelTuteur(txtTelTuteur.getText().trim());
                el.setTypeTuteur((String) cmbTypeTuteur.getSelectedItem());

                if (isEdit) {
                    eleveDAO.modifier(el);
                } else {
                    el.setDateCreation(LocalDate.now().toString());
                    int newId = eleveDAO.ajouter(el);
                    if (cmbClasseAdd.getSelectedIndex() >= 0 && classesDisponibles != null) {
                        Classe c = classesDisponibles.get(cmbClasseAdd.getSelectedIndex());
                        eleveDAO.affecterClasse(newId, c.getId(), mainFrame.getAnneeScolaireActuelle());
                    }
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
        dialog.add(tabs, BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void dialogAffecterClasse() {
        Eleve eleve = getEleveSelectionne();
        if (eleve == null)
            return;
        JDialog dialog = new JDialog(mainFrame, "Affecter une classe à " + eleve.getNomComplet(), true);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<String> cmbClasseF = new JComboBox<>();
        JComboBox<String> cmbAnneeF = new JComboBox<>();
        if (anneesDisponibles != null)
            for (String a : anneesDisponibles)
                cmbAnneeF.addItem(a);
        cmbAnneeF.setSelectedItem(mainFrame.getAnneeScolaireActuelle());
        @SuppressWarnings("unchecked")
        List<Classe>[] refClasses = (List<Classe>[]) new List[] { classesDisponibles };
        Runnable majClasses = () -> {
            cmbClasseF.removeAllItems();
            try {
                refClasses[0] = classeDAO.listerParAnnee((String) cmbAnneeF.getSelectedItem());
                for (Classe c : refClasses[0])
                    cmbClasseF.addItem(c.getNom() + " (" + c.getNiveauCode() + ")");
            } catch (Exception ignore) {
            }
        };
        majClasses.run();
        cmbAnneeF.addActionListener(e -> majClasses.run());
        ajouterLigne(form, gbc, 0, "Année scolaire :", cmbAnneeF);
        ajouterLigne(form, gbc, 1, "Classe :", cmbClasseF);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = UIFactory.boutonSucces("Affecter");
        JButton btnAnnul = UIFactory.boutonSecondaire("Annuler");
        btnAnnul.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(ev -> {
            if (cmbClasseF.getSelectedIndex() < 0 || refClasses[0] == null || refClasses[0].isEmpty())
                return;
            Classe classeChoisie = refClasses[0].get(cmbClasseF.getSelectedIndex());
            try {
                eleveDAO.affecterClasse(eleve.getId(), classeChoisie.getId(), (String) cmbAnneeF.getSelectedItem());
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

    private void afficherFiche() {
        Eleve e = getEleveSelectionne();
        if (e == null)
            return;
        JDialog dialog = new JDialog(mainFrame, "Fiche de l'élève : " + e.getNomComplet(), true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        JPanel content = new JPanel(new GridLayout(0, 2, 6, 8));
        content.setBorder(new EmptyBorder(20, 26, 20, 26));

        ajouterInfoFiche(content, "Matricule :", e.getMatricule());
        ajouterInfoFiche(content, "Nom complet :", e.getNomComplet());
        ajouterInfoFiche(content, "Sexe :", "M".equals(e.getSexe()) ? "Masculin" : "Féminin");
        ajouterInfoFiche(content, "Date de naissance :", e.getDateNaissance());
        ajouterInfoFiche(content, "Lieu de naissance :", e.getLieuNaissance());
        ajouterInfoFiche(content, "Adresse :", e.getAdresse());
        ajouterInfoFiche(content, "Classe :", e.getClasseNom() != null ? e.getClasseNom() : "Non inscrit");
        ajouterInfoFiche(content, "Année scolaire :", e.getAnneeScolaire());

        ajouterInfoFiche(content, "Nom du Père :", e.getNomPere());
        ajouterInfoFiche(content, "Tél Père :", e.getTelPere());
        ajouterInfoFiche(content, "Nom de la Mère :", e.getNomMere());
        ajouterInfoFiche(content, "Tél Mère :", e.getTelMere());
        ajouterInfoFiche(content, "Tuteur Principal :", e.getTypeTuteur());
        if (e.getNomTuteur() != null && !e.getNomTuteur().isEmpty()) {
            ajouterInfoFiche(content, "Nom Tuteur/Autre :", e.getNomTuteur());
            ajouterInfoFiche(content, "Tél Tuteur :", e.getTelTuteur());
        }

        JScrollPane sp = new JScrollPane(content);
        dialog.add(sp, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFermer = UIFactory.boutonSecondaire("Fermer");
        btnFermer.addActionListener(x -> dialog.dispose());
        bp.add(btnFermer);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void ajouterInfoFiche(JPanel p, String label, String valeur) {
        JLabel lbl = UIFactory.label(label);
        lbl.setFont(Constantes.FONT_BOLD);
        p.add(lbl);
        p.add(UIFactory.label(valeur != null && !valeur.isEmpty() ? valeur : "—"));
    }

    private void importerCSV() {
        JOptionPane.showMessageDialog(this,
                "Fonctionnalité d'importation adaptée aux nouveaux champs parentale disponible prochainement.",
                "Import CSV", JOptionPane.INFORMATION_MESSAGE);
    }

    private void supprimerSelectionne() {
        Eleve e = getEleveSelectionne();
        if (e == null)
            return;
        int rep = JOptionPane.showConfirmDialog(this, "Supprimer l'élève \"" + e.getNomComplet() + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            try {
                eleveDAO.supprimer(e.getId());
                rafraichir();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ajouterLigne(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        form.add(UIFactory.label(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(comp, gbc);
    }
}
