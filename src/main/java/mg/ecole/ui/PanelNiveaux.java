package mg.ecole.ui;

import mg.ecole.dao.NiveauDAO;
import mg.ecole.modele.Niveau;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel de gestion des niveaux scolaires.
 * Recherche multicritère + CRUD complet.
 */
public class PanelNiveaux extends JPanel {

    private final MainFrame mainFrame;
    private final NiveauDAO niveauDAO;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtRecherche;
    private List<Niveau> listeCourante;

    public PanelNiveaux(MainFrame mainFrame, NiveauDAO niveauDAO) {
        this.mainFrame = mainFrame;
        this.niveauDAO = niveauDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel lblTitre = UIFactory.labelTitre("Niveaux Scolaires", "levels.svg");
        JButton btnAjouter = UIFactory.boutonPrincipal("Ajouter");
        btnAjouter.setIcon(UIFactory.icone("plus.svg", 16));
        btnAjouter.addActionListener(e -> dialogAjouter());
        header.add(lblTitre, BorderLayout.WEST);
        header.add(btnAjouter, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Barre de recherche ───────────────────────────────────────────────
        JPanel barreRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barreRecherche.setOpaque(false);
        barreRecherche.setBorder(new EmptyBorder(0, 0, 12, 0));
        barreRecherche.add(UIFactory.label("Recherche :"));
        txtRecherche = UIFactory.champTexte(24);
        txtRecherche.setToolTipText("Rechercher par code ou libellé");
        txtRecherche.addActionListener(e -> rechercher());
        barreRecherche.add(txtRecherche);
        JButton btnRecherche = UIFactory.boutonPrincipal("Rechercher");
        btnRecherche.setIcon(UIFactory.icone("search.svg", 16));
        JButton btnReset = UIFactory.boutonSecondaire("");
        btnReset.setIcon(UIFactory.icone("refresh.svg", 16));
        btnReset.addActionListener(e -> {
            txtRecherche.setText("");
            rafraichir();
        });
        barreRecherche.add(btnRecherche);
        barreRecherche.add(btnReset);
        add(barreRecherche, BorderLayout.AFTER_LINE_ENDS);

        // ── Tableau ──────────────────────────────────────────────────────────
        String[] colonnes = { "#", "Code", "Libellé", "Ordre", "Description" };
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)));

        // Panneau central (recherche + table)
        JPanel centre = new JPanel(new BorderLayout(0, 0));
        centre.setOpaque(false);
        centre.add(barreRecherche, BorderLayout.NORTH);
        centre.add(scrollPane, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // ── Barre des boutons bas ─────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.setIcon(UIFactory.icone("edit.svg", 16));
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.setIcon(UIFactory.icone("delete.svg", 16));
        btnModifier.addActionListener(e -> modifierSelectionne());
        btnSupprimer.addActionListener(e -> supprimerSelectionne());
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    public void rafraichir() {
        try {
            listeCourante = niveauDAO.listerTous();
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechercher() {
        try {
            listeCourante = niveauDAO.rechercher(txtRecherche.getText());
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur recherche : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Niveau> niveaux) {
        tableModel.setRowCount(0);
        for (int i = 0; i < niveaux.size(); i++) {
            Niveau n = niveaux.get(i);
            tableModel.addRow(new Object[] { i + 1, n.getCode(), n.getLibelle(), n.getOrdre(), n.getDescription() });
        }
    }

    private Niveau getNiveauSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un niveau.", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        return listeCourante.get(modelRow);
    }

    // ─── Dialogs CRUD ────────────────────────────────────────────────────────

    private void dialogAjouter() {
        afficherDialogNiveau(null);
    }

    private void modifierSelectionne() {
        Niveau n = getNiveauSelectionne();
        if (n != null)
            afficherDialogNiveau(n);
    }

    private void afficherDialogNiveau(Niveau niveauExistant) {
        boolean isEdit = niveauExistant != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Modifier le niveau" : "Ajouter un niveau", true);
        dialog.setSize(450, 340);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCode = UIFactory.champTexte(10);
        JTextField txtLibelle = UIFactory.champTexte(24);
        JSpinner spnOrdre = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        JTextField txtDesc = UIFactory.champTexte(24);

        if (isEdit) {
            txtCode.setText(niveauExistant.getCode());
            txtLibelle.setText(niveauExistant.getLibelle());
            spnOrdre.setValue(niveauExistant.getOrdre());
            txtDesc.setText(niveauExistant.getDescription());
        }

        int row = 0;
        ajouterLigne(form, gbc, row++, "Code *", txtCode);
        ajouterLigne(form, gbc, row++, "Libellé *", txtLibelle);
        ajouterLigne(form, gbc, row++, "Ordre", spnOrdre);
        ajouterLigne(form, gbc, row, "Description", txtDesc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = UIFactory.boutonPrincipal(isEdit ? "Enregistrer" : "Ajouter");
        JButton btnAnnuler = UIFactory.boutonSecondaire("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String libelle = txtLibelle.getText().trim();
            if (code.isEmpty() || libelle.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Code et libellé sont obligatoires.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (niveauDAO.codeExiste(code, isEdit ? niveauExistant.getId() : -1)) {
                    JOptionPane.showMessageDialog(dialog, "Ce code existe déjà.", "Doublon",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Niveau n = isEdit ? niveauExistant : new Niveau();
                n.setCode(code.toUpperCase());
                n.setLibelle(libelle);
                n.setOrdre((Integer) spnOrdre.getValue());
                n.setDescription(txtDesc.getText().trim());
                if (isEdit) {
                    niveauDAO.modifier(n);
                    mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "MODIF NIVEAU",
                            "Mise à jour du niveau " + n.getCode());
                } else {
                    niveauDAO.ajouter(n);
                    mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "NOUVEAU NIVEAU",
                            "Création du niveau " + n.getCode());
                }
                rafraichir();
                dialog.dispose();
                JOptionPane.showMessageDialog(mainFrame, "Niveau " + (isEdit ? "modifié" : "ajouté") + " avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnAnnuler);
        btnPanel.add(btnOk);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void supprimerSelectionne() {
        Niveau n = getNiveauSelectionne();
        if (n == null)
            return;
        int rep = JOptionPane.showConfirmDialog(this,
                "Supprimer le niveau \"" + n.getLibelle() + "\" ?\nCette action est irréversible.",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            try {
                niveauDAO.supprimer(n.getId());
                mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "SUPPR NIVEAU",
                        "Suppression du niveau " + n.getCode());
                rafraichir();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Impossible de supprimer : " + e.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
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
