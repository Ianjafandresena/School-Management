package mg.ecole.ui;

import mg.ecole.dao.FraisScolaireDAO;
import mg.ecole.dao.NiveauDAO;
import mg.ecole.modele.FraisScolaire;
import mg.ecole.modele.Niveau;
import mg.ecole.modele.Paiement;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel de configuration des tarifs (Écolage, Droit, FRAM) par niveau.
 */
public class PanelFraisScolaires extends JPanel {

    private final FraisScolaireDAO fraisDAO;
    private final NiveauDAO niveauDAO;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Niveau> niveaux;
    private final JComboBox<String> cmbAnnee;

    public PanelFraisScolaires(MainFrame mainFrame, FraisScolaireDAO fraisDAO, NiveauDAO niveauDAO) {
        this.fraisDAO = fraisDAO;
        this.niveauDAO = niveauDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitre = UIFactory.labelTitre("Configuration des Frais Scolaires", "settings.svg");
        header.add(lblTitre, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        controls.setOpaque(false);

        cmbAnnee = new JComboBox<>();
        try {
            // Utilisation d'un combo simple pour l'année, alimenté par le MainFrame
            cmbAnnee.addItem(mainFrame.getAnneeScolaireActuelle());
        } catch (Exception ignore) {
        }

        JButton btnEnregistrer = UIFactory.boutonSucces("Enregistrer les tarifs");
        btnEnregistrer.setIcon(UIFactory.icone("accounting.svg", 16));
        btnEnregistrer.addActionListener(e -> enregistrer());

        controls.add(UIFactory.label("Année scolaire :"));
        controls.add(cmbAnnee);
        controls.add(btnEnregistrer);
        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Tableau ──────────────────────────────────────────────────────────
        String[] colonnes = { "Niveau", "Droits (Ar) *", "Écolage Mensuel (Ar) *", "FRAM (Ar) *",
                "Participation (Ar)" };
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // Désactivation de l'édition directe
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex > 0)
                    return Double.class;
                return String.class;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.setRowHeight(35);

        // Double-clic pour ouvrir le dialogue
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    modifierSelection();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Barre d'actions en bas
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnModifier = UIFactory.boutonPrincipal("Définir les montants");
        btnModifier.setIcon(UIFactory.icone("edit.svg", 18));
        btnModifier.addActionListener(e -> modifierSelection());

        bottomPanel.add(btnModifier, BorderLayout.WEST);

        JLabel lblNote = new JLabel(
                "<html><body>💡 <b>Note :</b> Sélectionnez un niveau puis cliquez sur 'Définir les montants' (ou double-cliquez sur la ligne).</body></html>");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bottomPanel.add(lblNote, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void modifierSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un niveau dans le tableau.", "Sélection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Niveau n = niveaux.get(table.convertRowIndexToModel(row));
        dialogFrais(n);
    }

    private void dialogFrais(Niveau niveau) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Définir les tarifs - " + niveau.getLibelle(), true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String annee = (String) cmbAnnee.getSelectedItem();
        int rowOrder = 0;

        JTextField txtDroits = UIFactory.champTexte(10);
        JTextField txtEcolage = UIFactory.champTexte(10);
        JTextField txtFram = UIFactory.champTexte(10);
        JTextField txtPart = UIFactory.champTexte(10);

        // Pré-remplir avec les valeurs actuelles du tableau
        int viewRow = table.getSelectedRow();
        txtDroits.setText(tableModel.getValueAt(viewRow, 1).toString());
        txtEcolage.setText(tableModel.getValueAt(viewRow, 2).toString());
        txtFram.setText(tableModel.getValueAt(viewRow, 3).toString());
        txtPart.setText(tableModel.getValueAt(viewRow, 4).toString());

        ajouterLigne(form, gbc, rowOrder++, "Droits d'inscription :", txtDroits);
        ajouterLigne(form, gbc, rowOrder++, "Écolage mensuel :", txtEcolage);
        ajouterLigne(form, gbc, rowOrder++, "Frais FRAM :", txtFram);
        ajouterLigne(form, gbc, rowOrder++, "Participation :", txtPart);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = UIFactory.boutonSucces("Valider");
        JButton btnAnnul = UIFactory.boutonSecondaire("Annuler");
        btnAnnul.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(e -> {
            try {
                double d = Double.parseDouble(txtDroits.getText().replace(",", ".").replace(" ", ""));
                double ec = Double.parseDouble(txtEcolage.getText().replace(",", ".").replace(" ", ""));
                double fr = Double.parseDouble(txtFram.getText().replace(",", ".").replace(" ", ""));
                double pa = Double.parseDouble(txtPart.getText().replace(",", ".").replace(" ", ""));

                sauvegarderTarif(annee, niveau.getId(), Paiement.TYPE_DROITS, d);
                sauvegarderTarif(annee, niveau.getId(), Paiement.TYPE_ECOLAGE, ec);
                sauvegarderTarif(annee, niveau.getId(), Paiement.TYPE_FRAM, fr);
                sauvegarderTarif(annee, niveau.getId(), Paiement.TYPE_PARTICIPATION, pa);

                rafraichir();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Veuillez saisir des montants valides (numériques).", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        bp.add(btnAnnul);
        bp.add(btnOk);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void ajouterLigne(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        form.add(UIFactory.label(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        form.add(comp, gbc);
    }

    public void rafraichir() {
        try {
            niveaux = niveauDAO.listerTous();
            String annee = (String) cmbAnnee.getSelectedItem();
            List<FraisScolaire> fraisExistants = fraisDAO.listerParAnnee(annee);

            // Mapper pour accès rapide niveauId_type -> montant
            Map<String, Double> mapFrais = new HashMap<>();
            for (FraisScolaire f : fraisExistants) {
                mapFrais.put(f.getNiveauId() + "_" + f.getTypePaiement(), f.getMontant());
            }

            tableModel.setRowCount(0);
            for (Niveau n : niveaux) {
                tableModel.addRow(new Object[] {
                        n.getLibelle(),
                        mapFrais.getOrDefault(n.getId() + "_" + Paiement.TYPE_DROITS, 0.0),
                        mapFrais.getOrDefault(n.getId() + "_" + Paiement.TYPE_ECOLAGE, 0.0),
                        mapFrais.getOrDefault(n.getId() + "_" + Paiement.TYPE_FRAM, 0.0),
                        mapFrais.getOrDefault(n.getId() + "_" + Paiement.TYPE_PARTICIPATION, 0.0)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enregistrer() {
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();

        String annee = (String) cmbAnnee.getSelectedItem();
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Niveau n = niveaux.get(i);
                sauvegarderTarif(annee, n.getId(), Paiement.TYPE_DROITS, (Double) tableModel.getValueAt(i, 1));
                sauvegarderTarif(annee, n.getId(), Paiement.TYPE_ECOLAGE, (Double) tableModel.getValueAt(i, 2));
                sauvegarderTarif(annee, n.getId(), Paiement.TYPE_FRAM, (Double) tableModel.getValueAt(i, 3));
                sauvegarderTarif(annee, n.getId(), Paiement.TYPE_PARTICIPATION, (Double) tableModel.getValueAt(i, 4));
            }
            JOptionPane.showMessageDialog(this, "Les tarifs ont été enregistrés avec succès.", "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sauvegarderTarif(String annee, int niveauId, String type, Double montant) throws SQLException {
        FraisScolaire f = new FraisScolaire();
        f.setAnneeScolaire(annee);
        f.setNiveauId(niveauId);
        f.setTypePaiement(type);
        f.setMontant(montant != null ? montant : 0.0);
        fraisDAO.enregistrer(f);
    }
}
