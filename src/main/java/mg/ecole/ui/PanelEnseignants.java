package mg.ecole.ui;

import mg.ecole.dao.EnseignantDAO;
import mg.ecole.modele.Enseignant;
import mg.ecole.util.UIFactory;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel de gestion des enseignants avec recherche multicritère.
 */
public class PanelEnseignants extends JPanel {

    private final MainFrame mainFrame;
    private final EnseignantDAO enseignantDAO;

    private final DefaultTableModel tableModel;
    private final JTable table;

    // Filtres de recherche
    private final JTextField txtNom;
    private final JComboBox<String> cmbSexe;

    private List<Enseignant> listeCourante;

    public PanelEnseignants(MainFrame mainFrame, EnseignantDAO enseignantDAO) {
        this.mainFrame = mainFrame;
        this.enseignantDAO = enseignantDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));
        headerPanel.add(UIFactory.labelTitre("Gestion des Enseignants", "enseignant.svg"), BorderLayout.WEST);
        JButton btnAjouter = UIFactory.boutonPrincipal("Ajouter");
        btnAjouter.setIcon(UIFactory.icone("plus.svg", 16));
        btnAjouter.addActionListener(e -> dialogEnseignant(null));
        headerPanel.add(btnAjouter, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ── Barre de recherche multicritère ───────────────────────────────────
        JPanel barreRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barreRecherche.setOpaque(false);
        barreRecherche.setBorder(BorderFactory.createTitledBorder(" Recherche multicritère "));

        txtNom = UIFactory.champTexte(18);
        cmbSexe = new JComboBox<>(new String[] { "Tous", "M", "F" });

        barreRecherche.add(UIFactory.label("Nom/Prénom :"));
        barreRecherche.add(txtNom);
        barreRecherche.add(UIFactory.label("Sexe :"));
        barreRecherche.add(cmbSexe);

        JButton btnRecherche = UIFactory.boutonPrincipal("Rechercher");
        btnRecherche.setIcon(UIFactory.icone("search.svg", 16));
        JButton btnReset = UIFactory.boutonSecondaire("");
        btnReset.setIcon(UIFactory.icone("refresh.svg", 16));
        btnRecherche.addActionListener(e -> rechercher());
        btnReset.addActionListener(e -> {
            txtNom.setText("");
            cmbSexe.setSelectedIndex(0);
            rafraichir();
        });
        barreRecherche.add(btnRecherche);
        barreRecherche.add(btnReset);

        // ── Tableau ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[] { "#", "Matricule", "Nom", "Prénom", "Sexe", "Téléphone", "Email",
                        "Date embauche" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centre = new JPanel(new BorderLayout(0, 8));
        centre.setOpaque(false);
        centre.add(barreRecherche, BorderLayout.NORTH);
        centre.add(scrollPane, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // ── Boutons bas ──────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.setOpaque(false);
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.setIcon(UIFactory.icone("edit.svg", 16));
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.setIcon(UIFactory.icone("delete.svg", 16));
        btnModifier.addActionListener(e -> dialogEnseignant(getEnseignantSelectionne()));
        btnSupprimer.addActionListener(e -> supprimerSelectionne());
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void rafraichir() {
        try {
            listeCourante = enseignantDAO.listerTous();
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechercher() {
        String sexe = cmbSexe.getSelectedIndex() == 0 ? null : (String) cmbSexe.getSelectedItem();
        try {
            listeCourante = enseignantDAO.listerAvecFiltre(
                    txtNom.getText(), null, sexe, false);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Enseignant> ens) {
        tableModel.setRowCount(0);
        for (int i = 0; i < ens.size(); i++) {
            Enseignant e = ens.get(i);
            tableModel.addRow(new Object[] { i + 1, e.getMatricule(), e.getNom(), e.getPrenom(),
                    e.getSexe(), e.getTelephone(), e.getEmail(),
                    e.getDateEmbauche() });
        }
    }

    private Enseignant getEnseignantSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un enseignant.", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return listeCourante.get(table.convertRowIndexToModel(row));
    }

    private void dialogEnseignant(Enseignant existant) {
        boolean isEdit = existant != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Modifier l'enseignant" : "Ajouter un enseignant", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 24, 8, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtMatricule = UIFactory.champTexte(14);
        JTextField txtNomF = UIFactory.champTexte(20);
        JTextField txtPrenomF = UIFactory.champTexte(20);
        JComboBox<String> cmbSexeF = new JComboBox<>(new String[] { "M", "F" });
        JTextField txtTelF = UIFactory.champTexte(16);
        JTextField txtEmailF = UIFactory.champTexte(22);
        DatePicker dpEmbauche = UIFactory.datePicker();

        if (isEdit) {
            txtMatricule.setText(existant.getMatricule());
            txtNomF.setText(existant.getNom());
            txtPrenomF.setText(existant.getPrenom());
            cmbSexeF.setSelectedItem(existant.getSexe());
            txtTelF.setText(existant.getTelephone());
            txtEmailF.setText(existant.getEmail());
            if (existant.getDateEmbauche() != null && !existant.getDateEmbauche().isEmpty()) {
                try {
                    dpEmbauche.setDate(LocalDate.parse(existant.getDateEmbauche()));
                } catch (Exception ignore) {
                }
            }
        } else {
            try {
                txtMatricule.setText(enseignantDAO.genererMatricule());
            } catch (Exception ignore) {
            }
        }

        int r = 0;
        ajouterLigne(form, gbc, r++, "Matricule *", txtMatricule);
        ajouterLigne(form, gbc, r++, "Nom *", txtNomF);
        ajouterLigne(form, gbc, r++, "Prénom *", txtPrenomF);
        ajouterLigne(form, gbc, r++, "Sexe *", cmbSexeF);
        ajouterLigne(form, gbc, r++, "Téléphone", txtTelF);
        ajouterLigne(form, gbc, r++, "Email", txtEmailF);
        ajouterLigne(form, gbc, r++, "Date embauche", dpEmbauche);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = UIFactory.boutonPrincipal(isEdit ? "Enregistrer" : "Ajouter");
        JButton btnAnnuler = UIFactory.boutonSecondaire("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(ev -> {
            String mat = txtMatricule.getText().trim(), nom = txtNomF.getText().trim(),
                    prenom = txtPrenomF.getText().trim();
            if (mat.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Matricule, Nom et Prénom sont obligatoires.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (enseignantDAO.matriculeExiste(mat, isEdit ? existant.getId() : -1)) {
                    JOptionPane.showMessageDialog(dialog, "Ce matricule existe déjà.", "Doublon",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Enseignant e = isEdit ? existant : new Enseignant();
                e.setMatricule(mat);
                e.setNom(nom.toUpperCase());
                e.setPrenom(prenom);
                e.setSexe((String) cmbSexeF.getSelectedItem());
                e.setTelephone(txtTelF.getText().trim());
                e.setEmail(txtEmailF.getText().trim());
                e.setDateEmbauche(dpEmbauche.getDateStringOrEmptyString());
                e.setActif(true);

                if (isEdit) {
                    enseignantDAO.modifier(e);
                } else {
                    enseignantDAO.ajouter(e);
                }
                rafraichir();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        bp.add(btnAnnuler);
        bp.add(btnOk);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void supprimerSelectionne() {
        Enseignant e = getEnseignantSelectionne();
        if (e == null)
            return;
        int rep = JOptionPane.showConfirmDialog(this,
                "Supprimer l'enseignant \"" + e.getNomComplet() + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            try {
                enseignantDAO.supprimer(e.getId());
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
