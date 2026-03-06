package mg.ecole.ui;

import mg.ecole.dao.ClasseDAO;
import mg.ecole.dao.EleveDAO;
import mg.ecole.dao.PaiementDAO;
import mg.ecole.modele.Classe;
import mg.ecole.modele.Eleve;
import mg.ecole.modele.Paiement;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.sql.SQLException;
import com.github.lgooddatepicker.components.DatePicker;
import java.util.*;
import java.util.List;

/**
 * Panel de gestion des paiements scolaires.
 * Saisie : choix élève → type → mois (si écolage) → montant → enregistrer.
 * Suivi : liste avec filtres multicritère, impayés, suppression.
 */
public class PanelPaiements extends JPanel {

    private final MainFrame mainFrame;
    private final PaiementDAO paiementDAO;
    private final EleveDAO eleveDAO;
    private final ClasseDAO classeDAO;

    // Filtres liste
    private final JComboBox<String> cmbAnneeFiltre;
    private final JComboBox<String> cmbTypeFiltre;
    private final JComboBox<String> cmbMoisFiltre;
    private final JComboBox<String> cmbClasseFiltre;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Paiement> listeCourante;
    private List<Classe> classesDisponibles;

    public PanelPaiements(MainFrame mainFrame, PaiementDAO paiementDAO,
            EleveDAO eleveDAO, ClasseDAO classeDAO) {
        this.mainFrame = mainFrame;
        this.paiementDAO = paiementDAO;
        this.eleveDAO = eleveDAO;
        this.classeDAO = classeDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 22, 16, 22));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        headerPanel.add(UIFactory.labelTitre("Gestion des Paiements", "payments.svg"), BorderLayout.WEST);

        JPanel btnHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnHeader.setOpaque(false);
        JButton btnNouveau = UIFactory.boutonPrincipal("Enregistrer paiement");
        btnNouveau.setIcon(UIFactory.icone("plus.svg", 16));
        JButton btnImpayes = UIFactory.boutonDanger("Impayés");
        btnImpayes.setIcon(UIFactory.icone("stats.svg", 16));
        btnNouveau.addActionListener(e -> dialogNouveauPaiement());
        btnImpayes.addActionListener(e -> dialogImpayes());
        btnHeader.add(btnImpayes);
        btnHeader.add(btnNouveau);
        headerPanel.add(btnHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ── Barre de filtres ─────────────────────────────────────────────────
        JPanel barreFiltre = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barreFiltre.setOpaque(false);
        barreFiltre.setBorder(BorderFactory.createTitledBorder(" Filtres de recherche "));

        cmbAnneeFiltre = new JComboBox<>();
        cmbTypeFiltre = new JComboBox<>(new String[] { "Tous types", Paiement.TYPE_DROITS, Paiement.TYPE_ECOLAGE,
                Paiement.TYPE_FRAM, Paiement.TYPE_PARTICIPATION });
        cmbMoisFiltre = new JComboBox<>();
        cmbClasseFiltre = new JComboBox<>();

        cmbMoisFiltre.addItem("Tous les mois");
        for (String m : Paiement.MOIS_ECOLAGE)
            cmbMoisFiltre.addItem(m);

        barreFiltre.add(UIFactory.label("Année :"));
        barreFiltre.add(cmbAnneeFiltre);
        barreFiltre.add(UIFactory.label("Type :"));
        barreFiltre.add(cmbTypeFiltre);
        barreFiltre.add(UIFactory.label("Mois :"));
        barreFiltre.add(cmbMoisFiltre);
        barreFiltre.add(UIFactory.label("Classe :"));
        barreFiltre.add(cmbClasseFiltre);

        JButton btnFiltrer = UIFactory.boutonPrincipal("Filtrer");
        btnFiltrer.setIcon(UIFactory.icone("search.svg", 16));
        JButton btnReset = UIFactory.boutonSecondaire("");
        btnReset.setIcon(UIFactory.icone("refresh.svg", 16));
        btnFiltrer.addActionListener(e -> filtrer());
        btnReset.addActionListener(e -> {
            cmbTypeFiltre.setSelectedIndex(0);
            cmbMoisFiltre.setSelectedIndex(0);
            cmbClasseFiltre.setSelectedIndex(0);
            rafraichir();
        });
        barreFiltre.add(btnFiltrer);
        barreFiltre.add(btnReset);

        // ── Tableau liste des paiements ───────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[] { "#", "Réf", "Élève", "Classe", "Type", "Mois", "Montant", "Date", "Mode" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(table);

        // Label total
        JLabel lblTotal = new JLabel("Total : 0 Ar");
        lblTotal.setFont(Constantes.FONT_BOLD);
        lblTotal.setForeground(Constantes.COULEUR_SECONDAIRE);
        this.lblTotal = lblTotal;

        JPanel centre = new JPanel(new BorderLayout(0, 6));
        centre.setOpaque(false);
        centre.add(barreFiltre, BorderLayout.NORTH);
        centre.add(scrollPane, BorderLayout.CENTER);
        centre.add(lblTotal, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        // ── Boutons bas ──────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.setOpaque(false);
        JButton btnRecu = UIFactory.boutonSecondaire("Imprimer reçu");
        btnRecu.setIcon(UIFactory.icone("fiche.svg", 16));
        JButton btnSuppr = UIFactory.boutonDanger("Supprimer");
        btnSuppr.setIcon(UIFactory.icone("delete.svg", 16));
        btnRecu.addActionListener(e -> imprimerRecu());
        btnSuppr.addActionListener(e -> supprimerSelectionne());
        btnPanel.add(btnRecu);
        btnRecu.addActionListener(e -> imprimerRecu());
        btnSuppr.addActionListener(e -> supprimerSelectionne());
        btnPanel.add(btnRecu);
        btnPanel.add(btnSuppr);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JLabel lblTotal;

    // ─── Chargement ──────────────────────────────────────────────────────────

    public void rafraichir() {
        try {
            String annee = mainFrame.getAnneeScolaireActuelle();
            classesDisponibles = classeDAO.listerParAnnee(annee);

            // Remplir combo années
            List<String> annees = classeDAO.listerAnneesScolaires();
            cmbAnneeFiltre.removeAllItems();
            if (!annees.contains(annee))
                annees.add(0, annee);
            for (String a : annees)
                cmbAnneeFiltre.addItem(a);
            cmbAnneeFiltre.setSelectedItem(annee);

            // Remplir combo classes
            cmbClasseFiltre.removeAllItems();
            cmbClasseFiltre.addItem("Toutes les classes");
            for (Classe c : classesDisponibles)
                cmbClasseFiltre.addItem(c.getNom());

            // Charger les paiements
            listeCourante = paiementDAO.listerAvecFiltre(null, null, annee, null, null);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrer() {
        try {
            String annee = (String) cmbAnneeFiltre.getSelectedItem();
            String type = cmbTypeFiltre.getSelectedIndex() == 0 ? null : (String) cmbTypeFiltre.getSelectedItem();
            String mois = cmbMoisFiltre.getSelectedIndex() == 0 ? null : (String) cmbMoisFiltre.getSelectedItem();
            Integer classeId = null;
            if (cmbClasseFiltre.getSelectedIndex() > 0 && classesDisponibles != null)
                classeId = classesDisponibles.get(cmbClasseFiltre.getSelectedIndex() - 1).getId();
            listeCourante = paiementDAO.listerAvecFiltre(null, classeId, annee, type, mois);
            remplirTable(listeCourante);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Paiement> paiements) {
        tableModel.setRowCount(0);
        double total = 0;
        for (int i = 0; i < paiements.size(); i++) {
            Paiement p = paiements.get(i);
            total += p.getMontant();
            tableModel.addRow(new Object[] {
                    i + 1, p.getReference(), p.getEleveNomComplet(),
                    p.getClasseNom(), p.getTypePaiement(), p.getMois() != null ? p.getMois() : "",
                    String.format("%,.0f Ar", p.getMontant()),
                    p.getDatePaiement(), p.getModePaiement()
            });
        }
        lblTotal.setText(String.format("Total : %,.0f Ar  |  %d paiement(s)", total, paiements.size()));
    }

    // ─── Dialog nouveau paiement ─────────────────────────────────────────────

    private void dialogNouveauPaiement() {
        JDialog dialog = new JDialog(mainFrame, "Enregistrer un paiement", true);
        dialog.setSize(580, 540);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 26, 10, 26));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ─ Sélection de l'année
        JComboBox<String> cmbAnnee = new JComboBox<>();
        try {
            List<String> an = classeDAO.listerAnneesScolaires();
            for (String a : an)
                cmbAnnee.addItem(a);
        } catch (Exception ignore) {
        }
        cmbAnnee.setSelectedItem(mainFrame.getAnneeScolaireActuelle());

        // ─ Sélection de la classe
        JComboBox<String> cmbClasse = new JComboBox<>();
        @SuppressWarnings("unchecked")
        List<Classe>[] refClasses = (List<Classe>[]) new List[] { classesDisponibles };
        Runnable chargerClasses = () -> {
            cmbClasse.removeAllItems();
            String annee = (String) cmbAnnee.getSelectedItem();
            try {
                refClasses[0] = classeDAO.listerParAnnee(annee);
                for (Classe c : refClasses[0])
                    cmbClasse.addItem(c.getNom());
            } catch (Exception ignore) {
            }
        };
        chargerClasses.run();
        cmbAnnee.addActionListener(e -> chargerClasses.run());

        // ─ Sélection de l'élève dans la classe choisie
        JComboBox<String> cmbEleve = new JComboBox<>();
        @SuppressWarnings("unchecked")
        List<Eleve>[] refEleves = (List<Eleve>[]) new List[] { new ArrayList<>() };
        Runnable chargerEleves = () -> {
            cmbEleve.removeAllItems();
            if (cmbClasse.getSelectedIndex() < 0 || refClasses[0] == null || refClasses[0].isEmpty())
                return;
            Classe c = refClasses[0].get(cmbClasse.getSelectedIndex());
            String annee = (String) cmbAnnee.getSelectedItem();
            try {
                refEleves[0] = eleveDAO.listerAvecFiltre(null, null, c.getId(), annee);
                for (Eleve e : refEleves[0])
                    cmbEleve.addItem(e.getMatricule() + " - " + e.getNomComplet());
            } catch (Exception ignore) {
            }
        };
        cmbClasse.addActionListener(e -> chargerEleves.run());
        chargerEleves.run();

        // ─ Type de paiement
        JComboBox<String> cmbType = new JComboBox<>(new String[] {
                Paiement.TYPE_DROITS, Paiement.TYPE_ECOLAGE, Paiement.TYPE_FRAM, Paiement.TYPE_PARTICIPATION });
        cmbType.setSelectedItem(Paiement.TYPE_ECOLAGE);

        // ── Montant
        JTextField txtMontant = UIFactory.champTexte(14);
        txtMontant.setToolTipText("Montant en Ariary");

        // ─ Mois (visible seulement pour ECOLAGE)
        JComboBox<String> cmbMois = new JComboBox<>(Paiement.MOIS_ECOLAGE);
        // Sélectionner le mois courant
        String moisActuel = obtenirMoisActuel();
        if (moisActuel != null)
            cmbMois.setSelectedItem(moisActuel);
        JLabel lblMoisLabel = UIFactory.label("Mois d'écolage *");

        // --- Logique de chargement auto du montant exigé ---
        final double[] montantExige = { 0.0 };
        Runnable actualiserMontantDefaut = () -> {
            if (cmbEleve.getSelectedIndex() < 0 || refEleves[0].isEmpty())
                return;
            Eleve e = refEleves[0].get(cmbEleve.getSelectedIndex());
            String type = (String) cmbType.getSelectedItem();
            String annee = (String) cmbAnnee.getSelectedItem();
            try {
                // On récupère le montant configuré pour le niveau de l'élève
                double du = mainFrame.getFraisDAO().getMontant(annee, e.getNiveauId(), type);
                montantExige[0] = du;
                txtMontant.setText(String.format("%.0f", du));
                txtMontant.setForeground(Color.BLACK);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        cmbEleve.addActionListener(e -> actualiserMontantDefaut.run());
        cmbType.addActionListener(e -> {
            boolean isEcolage = Paiement.TYPE_ECOLAGE.equals(cmbType.getSelectedItem());
            cmbMois.setVisible(isEcolage);
            lblMoisLabel.setVisible(isEcolage);
            actualiserMontantDefaut.run();
            // Redimensionner le dialogue si nécessaire (selon le layout)
            dialog.pack();
            dialog.setSize(580, isEcolage ? 540 : 480);
        });

        // Validation en temps réel du montant (rouge si inférieur)
        txtMontant.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                try {
                    double saisi = Double.parseDouble(txtMontant.getText().replace(",", ""));
                    if (saisi < montantExige[0]) {
                        txtMontant.setForeground(Color.RED);
                    } else {
                        txtMontant.setForeground(new Color(0, 150, 0)); // Vert si ok
                    }
                } catch (Exception ex) {
                    txtMontant.setForeground(Color.RED);
                }
            }
        });

        // ── Date
        DatePicker dpDate = UIFactory.datePicker();
        dpDate.setDateToToday();

        // ─ Mode
        JComboBox<String> cmbMode = new JComboBox<>(Paiement.MODES);

        // ─ Note
        JTextField txtNote = UIFactory.champTexte(22);

        int r = 0;
        ajouterLigne(form, gbc, r++, "Année scolaire *", cmbAnnee);
        ajouterLigne(form, gbc, r++, "Classe *", cmbClasse);
        ajouterLigne(form, gbc, r++, "Élève *", cmbEleve);
        ajouterLigne(form, gbc, r++, "Type paiement *", cmbType);

        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.weightx = 0.3;
        form.add(lblMoisLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(cmbMois, gbc);
        r++;

        ajouterLigne(form, gbc, r++, "Montant (Ar) *", txtMontant);
        ajouterLigne(form, gbc, r++, "Date paiement *", dpDate);
        ajouterLigne(form, gbc, r++, "Mode paiement", cmbMode);
        ajouterLigne(form, gbc, r, "Note", txtNote);

        // ─ Boutons
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEnreg = UIFactory.boutonSucces("Enregistrer");
        btnEnreg.setIcon(UIFactory.icone("accounting.svg", 16));
        JButton btnAnnul = UIFactory.boutonSecondaire("Annuler");
        btnAnnul.addActionListener(e -> dialog.dispose());
        btnEnreg.addActionListener(ev -> {
            // Validation
            if (cmbClasse.getSelectedIndex() < 0 || refClasses[0] == null || refClasses[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Choisissez une classe.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cmbEleve.getSelectedIndex() < 0 || refEleves[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Choisissez un élève.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String montantTxt = txtMontant.getText().trim();
            if (montantTxt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Saisissez le montant.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            double montant;
            try {
                montant = Double.parseDouble(montantTxt.replace(",", ""));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Montant invalide.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Eleve eleve = refEleves[0].get(cmbEleve.getSelectedIndex());
            Classe classe = refClasses[0].get(cmbClasse.getSelectedIndex());
            String type = (String) cmbType.getSelectedItem();
            String annee = (String) cmbAnnee.getSelectedItem();
            String mois = Paiement.TYPE_ECOLAGE.equals(type) ? (String) cmbMois.getSelectedItem() : null;

            // Alerte si montant inférieur
            if (montant < montantExige[0]) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Le montant saisi (" + montant + " Ar) est inférieur au montant exigé (" + montantExige[0]
                                + " Ar).\nConfirmer quand même le paiement partiel ?",
                        "Paiement Insuffisant", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION)
                    return;
            }

            // Vérification doublon / dépassement
            try {
                // Calculer ce qui a déjà été payé pour ce mois/type
                double dejaPaye = paiementDAO.getTotalPaye(eleve.getId(), annee, type, mois);
                if (dejaPaye + montant > montantExige[0] && montantExige[0] > 0) {
                    String msg = String.format(
                            "Erreur : Impossible d'enregistrer ce paiement.\n" +
                                    "- Montant déjà payé : %,.0f Ar\n" +
                                    "- Nouveau montant : %,.0f Ar\n" +
                                    "- Total (%,.0f Ar) dépasserait le montant exigé (%,.0f Ar) pour %s.",
                            dejaPaye, montant, (dejaPaye + montant), montantExige[0], type);

                    JOptionPane.showMessageDialog(dialog, msg, "Dépassement interdit", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                btnEnreg.setEnabled(false); // Éviter double clic
                Paiement p = new Paiement();
                p.setEleveId(eleve.getId());
                p.setClasseId(classe.getId());
                p.setAnneeScolaire(annee);
                p.setTypePaiement(type);
                p.setMois(mois);
                p.setMontant(montant);
                p.setDatePaiement(dpDate.getDateStringOrEmptyString());
                p.setModePaiement((String) cmbMode.getSelectedItem());
                p.setNote(txtNote.getText().trim());
                p.setReference(paiementDAO.genererNumeroRecu(annee));

                paiementDAO.ajouter(p);
                mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "PAIEMENT", "Saisie de " + p.getMontant()
                        + " Ar (" + p.getTypePaiement() + ") pour " + eleve.getNomComplet());
                rafraichir();
                dialog.dispose();

                // Proposer d'imprimer le reçu
                int imprim = JOptionPane.showConfirmDialog(mainFrame,
                        "Paiement enregistré !\nRéférence : " + p.getReference() + "\nVoulez-vous imprimer le reçu ?",
                        "Paiement enregistré", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (imprim == JOptionPane.YES_OPTION)
                    afficherRecu(p, eleve, classe);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        bp.add(btnAnnul);
        bp.add(btnEnreg);
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ─── Reçu de paiement ────────────────────────────────────────────────────

    private void afficherRecu(Paiement p, Eleve eleve, Classe classe) {
        JDialog recu = new JDialog(mainFrame, "Reçu de paiement - " + p.getReference(), true);
        recu.setSize(420, 380);
        recu.setLocationRelativeTo(mainFrame);

        JTextArea txt = new JTextArea();
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setEditable(false);
        String nomEcole = mainFrame.getNomEcole();
        String devise = mainFrame.getDevise();

        txt.setText(
                "╔══════════════════════════════════════╗\n" +
                        "║         " + String.format("%-30s", nomEcole) + "║\n" +
                        "║              REÇU DE PAIEMENT        ║\n" +
                        "╠══════════════════════════════════════╣\n" +
                        "║ Réf   : " + String.format("%-29s", p.getReference()) + "║\n" +
                        "║ Date  : " + String.format("%-29s", p.getDatePaiement()) + "║\n" +
                        "╠══════════════════════════════════════╣\n" +
                        "║ Élève : " + String.format("%-29s", eleve.getNomComplet()) + "║\n" +
                        "║ Mat.  : " + String.format("%-29s", eleve.getMatricule()) + "║\n" +
                        "║ Classe: " + String.format("%-29s", classe.getNom()) + "║\n" +
                        "║ Année : " + String.format("%-29s", p.getAnneeScolaire()) + "║\n" +
                        "╠══════════════════════════════════════╣\n" +
                        "║ Type  : " + String.format("%-29s", p.getTypePaiement()) + "║\n" +
                        (p.getMois() != null ? "║ Mois  : " + String.format("%-29s", p.getMois()) + "║\n" : "") +
                        "║ Mode  : " + String.format("%-29s", p.getModePaiement()) + "║\n" +
                        "╠══════════════════════════════════════╣\n" +
                        "║ MONTANT : " + String.format("%-27s", String.format("%,.0f %s", p.getMontant(), devise))
                        + "║\n" +
                        "╚══════════════════════════════════════╝\n" +
                        "\n      Signature : ___________________");

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnImpr = UIFactory.boutonPrincipal("Imprimer");
        btnImpr.setIcon(UIFactory.icone("print.svg", 16));
        JButton btnFerm = UIFactory.boutonSecondaire("Fermer");
        btnImpr.addActionListener(e -> {
            try {
                txt.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(recu, "Erreur impression: " + ex.getMessage());
            }
        });
        btnFerm.addActionListener(e -> recu.dispose());
        bp.add(btnImpr);
        bp.add(btnFerm);

        recu.setLayout(new BorderLayout());
        recu.add(new JScrollPane(txt), BorderLayout.CENTER);
        recu.add(bp, BorderLayout.SOUTH);
        recu.setVisible(true);
    }

    private void imprimerRecu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un paiement.", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Paiement p = listeCourante.get(table.convertRowIndexToModel(row));
        JOptionPane.showMessageDialog(this,
                "Référence : " + p.getReference() + "\n" +
                        "Élève : " + p.getEleveNomComplet() + "\n" +
                        "Type : " + p.getLibelleComplet() + "\n" +
                        "Montant : " + String.format("%,.0f Ar", p.getMontant()) + "\n" +
                        "Date : " + p.getDatePaiement(),
                "Détail du paiement", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Impayés ────────────────────────────────────────────────────────────

    private void dialogImpayes() {
        JDialog dialog = new JDialog(mainFrame, "⚠ Élèves non à jour - Écolage", true);
        dialog.setSize(640, 420);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);
        JComboBox<String> cmbMoisImp = new JComboBox<>(Paiement.MOIS_ECOLAGE);
        cmbMoisImp.setSelectedItem(obtenirMoisActuel());
        JButton btnRech = UIFactory.boutonPrincipal("Rechercher");
        btnRech.setIcon(UIFactory.icone("search.svg", 16));
        top.add(UIFactory.label("Mois :"));
        top.add(cmbMoisImp);
        top.add(btnRech);

        DefaultTableModel modelImp = new DefaultTableModel(
                new String[] { "#", "Matricule", "Nom", "Prénom", "Classe" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tblImp = UIFactory.creerTable();
        tblImp.setModel(modelImp);

        btnRech.addActionListener(ev -> {
            modelImp.setRowCount(0);
            String mois = (String) cmbMoisImp.getSelectedItem();
            String annee = mainFrame.getAnneeScolaireActuelle();
            try {
                List<Map<String, Object>> impayes = paiementDAO.elevesImpayesEcolage(annee, mois);
                for (int i = 0; i < impayes.size(); i++) {
                    Map<String, Object> row = impayes.get(i);
                    modelImp.addRow(new Object[] { i + 1, row.get("matricule"), row.get("nom"), row.get("prenom"),
                            row.get("classe") });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnRech.doClick();

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFerm = UIFactory.boutonSecondaire("Fermer");
        btnFerm.addActionListener(e -> dialog.dispose());
        bp.add(btnFerm);

        dialog.add(top, BorderLayout.NORTH);
        dialog.add(new JScrollPane(tblImp), BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void supprimerSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un paiement.", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Paiement p = listeCourante.get(table.convertRowIndexToModel(row));
        int rep = JOptionPane.showConfirmDialog(this,
                "Supprimer le paiement [" + p.getReference() + "] de " + p.getEleveNomComplet() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            try {
                paiementDAO.supprimer(p.getId());
                mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "SUPPRESSION PAIEMENT",
                        "Suppression paiement " + p.getReference() + " de " + p.getMontant() + " Ar");
                rafraichir();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String obtenirMoisActuel() {
        int moisNum = LocalDate.now().getMonthValue();
        String[] moisFr = { null, "JANVIER", "FEVRIER", "MARS", "AVRIL", "MAI", "JUIN",
                null, null, "SEPTEMBRE", "OCTOBRE", "NOVEMBRE" };
        if (moisNum < moisFr.length && moisFr[moisNum] != null)
            return moisFr[moisNum];
        return "SEPTEMBRE";
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
