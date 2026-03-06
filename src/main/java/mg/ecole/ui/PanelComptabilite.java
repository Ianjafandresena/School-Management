package mg.ecole.ui;

import mg.ecole.dao.ClasseDAO;
import mg.ecole.dao.EleveDAO;
import mg.ecole.dao.FraisScolaireDAO;
import mg.ecole.dao.NiveauDAO;
import mg.ecole.dao.PaiementDAO;
import mg.ecole.modele.FraisScolaire;
import mg.ecole.modele.Niveau;
import mg.ecole.modele.Paiement;
import mg.ecole.util.UIFactory;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel Comptabilité / Recettes (Conforme au document officiel).
 * Affiche un tableau croisé : lignes = Types/Mois,
 * colonnes = Niveaux Scolaires (Double colonne : Attendu | Payé).
 */
public class PanelComptabilite extends JPanel {

    private final PaiementDAO paiementDAO;
    private final NiveauDAO niveauDAO;
    private final FraisScolaireDAO fraisDAO;
    private final EleveDAO eleveDAO;

    private final JComboBox<String> cmbAnnee;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public PanelComptabilite(MainFrame mainFrame, PaiementDAO paiementDAO, ClasseDAO classeDAO, NiveauDAO niveauDAO) {
        this.paiementDAO = paiementDAO;
        this.niveauDAO = niveauDAO;
        this.fraisDAO = mainFrame.getFraisDAO();
        this.eleveDAO = new EleveDAO();

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 22, 16, 22));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(UIFactory.labelTitre("Comptabilité & Recettes", "accounting.svg"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        cmbAnnee = new JComboBox<>();
        try {
            List<String> annees = classeDAO.listerAnneesScolaires();
            for (String a : annees)
                cmbAnnee.addItem(a);
            cmbAnnee.setSelectedItem(mainFrame.getAnneeScolaireActuelle());
        } catch (Exception ignore) {
        }

        JButton btnActualiser = UIFactory.boutonPrincipal("Actualiser");
        btnActualiser.setIcon(UIFactory.icone("refresh.svg", 16));
        JButton btnExport = UIFactory.boutonSecondaire("Export Excel");
        btnExport.setIcon(UIFactory.icone("accounting.svg", 16));

        btnActualiser.addActionListener(e -> rafraichir());
        btnExport.addActionListener(e -> {
            exportExcel();
            mainFrame.getJournalDAO().log(mainFrame.getCurrentUser(), "EXPORT", "Exportation du tableau de recettes");
        });

        btnPanel.add(UIFactory.label("Année scolaire :"));
        btnPanel.add(cmbAnnee);
        btnPanel.add(btnActualiser);
        btnPanel.add(btnExport);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Tableau Matriciel ────────────────────────────────────────────────
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void rafraichir() {
        String annee = (String) cmbAnnee.getSelectedItem();
        if (annee == null)
            return;

        try {
            List<Niveau> niveaux = niveauDAO.listerTous();
            Map<Integer, Map<String, Double>> dataPaye = paiementDAO.totauxParNiveauEtType(annee);
            Map<Integer, Integer> effectifs = eleveDAO.compterElevesParNiveau(annee);
            List<FraisScolaire> listeFrais = fraisDAO.listerParAnnee(annee);

            // Mapper les frais : niveauId_type -> montant
            Map<String, Double> mapFrais = new HashMap<>();
            for (FraisScolaire f : listeFrais) {
                mapFrais.put(f.getNiveauId() + "_" + f.getTypePaiement(), f.getMontant());
            }

            // Préparation des colonnes : RECETTES | MOIS | [Niveau1 Attendu] | [Niveau1
            // Payé] | ...
            tableModel.setColumnCount(0);
            tableModel.addColumn("RECETTES");
            tableModel.addColumn("MOIS");
            for (Niveau n : niveaux) {
                tableModel.addColumn(n.getCode() + " (Attendu)");
                tableModel.addColumn(n.getCode() + " (Payé)");
            }
            tableModel.addColumn("TOTAL ATTENDU");
            tableModel.addColumn("TOTAL PAYÉ");

            tableModel.setRowCount(0);

            // 1. DROITS
            addRowMatrix(niveaux, effectifs, mapFrais, dataPaye, "DROITS", null, "DROITS", null);
            addTotalRow("DROITS", "Total DROITS");

            // 2. ECOLAGE
            for (String mois : Paiement.MOIS_ECOLAGE) {
                addRowMatrix(niveaux, effectifs, mapFrais, dataPaye, "ECOLAGE", mois, "ECOLAGE", "ECOLAGE|" + mois);
            }
            addTotalRow("ECOLAGE", "Total ECOLAGE");

            // 3. FRAM
            addRowMatrix(niveaux, effectifs, mapFrais, dataPaye, "FRAM", null, "FRAM", null);
            addTotalRow("FRAM", "Total FRAM");

            // 4. PARTICIPATION
            addRowMatrix(niveaux, effectifs, mapFrais, dataPaye, "PARTICIPATION", null, "PARTICIPATION", null);
            addTotalRow("PARTICIPATION", "Total PARTICIPATION");

            // 5. RÉSULTAT FINAL
            addGlobalResultRow();

            configurerRendu();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addRowMatrix(List<Niveau> niveaux, Map<Integer, Integer> effectifs,
            Map<String, Double> mapFrais, Map<Integer, Map<String, Double>> dataPaye,
            String cat, String subcat, String typeCle, String compositeCle) {

        Object[] row = new Object[niveaux.size() * 2 + 4];
        row[0] = cat;
        row[1] = (subcat == null) ? "" : subcat;

        String cle = (compositeCle != null) ? compositeCle : typeCle;
        double totalAttendu = 0;
        double totalPaye = 0;

        for (int i = 0; i < niveaux.size(); i++) {
            int niveauId = niveaux.get(i).getId();
            int nbEleves = effectifs.getOrDefault(niveauId, 0);
            double tarif = mapFrais.getOrDefault(niveauId + "_" + typeCle, 0.0);

            double attendu = nbEleves * tarif;
            double paye = 0;
            if (dataPaye.containsKey(niveauId)) {
                paye = dataPaye.get(niveauId).getOrDefault(cle, 0.0);
            }

            row[2 + i * 2] = attendu;
            row[3 + i * 2] = paye;

            totalAttendu += attendu;
            totalPaye += paye;
        }

        row[row.length - 2] = totalAttendu;
        row[row.length - 1] = totalPaye;
        tableModel.addRow(row);
    }

    private void addTotalRow(String cat, String label) {
        int colCount = tableModel.getColumnCount();
        Object[] total = new Object[colCount];
        total[0] = cat;
        total[1] = label;

        for (int c = 2; c < colCount; c++) {
            double sum = 0;
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                if (cat.equals(tableModel.getValueAt(r, 0))
                        && !tableModel.getValueAt(r, 1).toString().startsWith("Total")) {
                    sum += (double) tableModel.getValueAt(r, c);
                }
            }
            total[c] = sum;
        }
        tableModel.addRow(total);
    }

    private void addGlobalResultRow() {
        int colCount = tableModel.getColumnCount();
        Object[] global = new Object[colCount];
        global[0] = "RÉSULTAT";
        global[1] = "Total général";

        for (int c = 2; c < colCount; c++) {
            double sum = 0;
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                String label = tableModel.getValueAt(r, 1).toString();
                if (label.startsWith("Total")) {
                    sum += (double) tableModel.getValueAt(r, c);
                }
            }
            global[c] = sum;
        }
        tableModel.addRow(global);
    }

    private void configurerRendu() {
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    setText(String.format("%,.0f Ar", ((Number) value).doubleValue()));
                    setHorizontalAlignment(JLabel.RIGHT);
                } else
                    super.setValue(value);
            }
        };

        DefaultTableCellRenderer boldRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object labelObj = table.getValueAt(row, 1);
                String label = (labelObj != null) ? labelObj.toString() : "";

                if (!isSelected) {
                    if (label.startsWith("Total") || label.equals("Total général")) {
                        c.setFont(c.getFont().deriveFont(java.awt.Font.BOLD));
                        c.setBackground(new java.awt.Color(0x38, 0x3E, 0x48));
                        c.setForeground(java.awt.Color.WHITE);
                    } else {
                        c.setBackground(new java.awt.Color(0x2D, 0x32, 0x3B));
                        c.setForeground(new java.awt.Color(0xD1, 0xD5, 0xDB));
                    }
                    // Alternance
                    if (column >= 2 && column < table.getColumnCount() - 2 && column % 2 != 0) {
                        c.setBackground(new java.awt.Color(0x32, 0x38, 0x43));
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(i < 2 ? 140 : 100);
            if (i >= 2)
                table.getColumnModel().getColumn(i).setCellRenderer(currencyRenderer);
            else
                table.getColumnModel().getColumn(i).setCellRenderer(boldRenderer);
        }
    }

    private void exportExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Aucune donnée à exporter.", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer l'export Excel Officiel");
        fileChooser.setSelectedFile(new File("Recettes_Officiel_" + cmbAnnee.getSelectedItem() + ".xlsx"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream fileOut = new FileOutputStream(fileToSave)) {

            Sheet sheet = workbook.createSheet("Recettes " + cmbAnnee.getSelectedItem());

            // --- Styles ---
            CellStyle styleHeader = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontH = workbook.createFont();
            fontH.setBold(true);
            styleHeader.setFont(fontH);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            styleHeader.setBorderBottom(BorderStyle.THIN);
            styleHeader.setBorderTop(BorderStyle.THIN);
            styleHeader.setBorderLeft(BorderStyle.THIN);
            styleHeader.setBorderRight(BorderStyle.THIN);
            styleHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleCurrency = workbook.createCellStyle();
            styleCurrency.setDataFormat(workbook.createDataFormat().getFormat("#,##0 \"Ar\""));
            styleCurrency.setBorderBottom(BorderStyle.THIN);
            styleCurrency.setBorderLeft(BorderStyle.THIN);
            styleCurrency.setBorderRight(BorderStyle.THIN);

            CellStyle styleBold = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontB = workbook.createFont();
            fontB.setBold(true);
            styleBold.setFont(fontB);
            styleBold.setDataFormat(workbook.createDataFormat().getFormat("#,##0 \"Ar\""));
            styleBold.setBorderBottom(BorderStyle.THIN);
            styleBold.setBorderLeft(BorderStyle.THIN);
            styleBold.setBorderRight(BorderStyle.THIN);
            styleBold.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            styleBold.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // --- En-têtes (Fusionnés) ---
            Row rowH1 = sheet.createRow(0);
            Row rowH2 = sheet.createRow(1);

            Cell cRec = rowH1.createCell(0);
            cRec.setCellValue("RECETTES");
            cRec.setCellStyle(styleHeader);
            Cell cMois = rowH1.createCell(1);
            cMois.setCellValue("MOIS");
            cMois.setCellStyle(styleHeader);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

            int col = 2;
            int nbNiveaux = (tableModel.getColumnCount() - 4) / 2;
            for (int i = 0; i < nbNiveaux; i++) {
                String fullHeader = tableModel.getColumnName(2 + i * 2);
                String levelCode = fullHeader.substring(0, fullHeader.indexOf(" "));

                Cell cLevel = rowH1.createCell(col);
                cLevel.setCellValue("CLASSE " + levelCode);
                cLevel.setCellStyle(styleHeader);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 1));

                Cell cVal = rowH2.createCell(col);
                cVal.setCellValue("Valeurs");
                cVal.setCellStyle(styleHeader);
                Cell cPay = rowH2.createCell(col + 1);
                cPay.setCellValue("Déjà payé");
                cPay.setCellStyle(styleHeader);
                col += 2;
            }

            Cell cTotA = rowH1.createCell(col);
            cTotA.setCellValue("TOTAL PROVISION");
            cTotA.setCellStyle(styleHeader);
            Cell cTotP = rowH1.createCell(col + 1);
            cTotP.setCellValue("TOTAL RÉEL");
            cTotP.setCellStyle(styleHeader);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, col + 1, col + 1));

            // --- Données ---
            int startEcolage = -1;
            int lastEcolage = -1;

            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Row excelRow = sheet.createRow(r + 2);
                Object catObj = tableModel.getValueAt(r, 0);
                String cat = catObj != null ? catObj.toString() : "";
                String label = tableModel.getValueAt(r, 1).toString();
                boolean isTotal = label.startsWith("Total");

                if ("ECOLAGE".equals(cat) && !isTotal) {
                    if (startEcolage == -1)
                        startEcolage = r + 2;
                    lastEcolage = r + 2;
                }

                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Cell cell = excelRow.createCell(c);
                    Object val = tableModel.getValueAt(r, c);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                        cell.setCellStyle(isTotal ? styleBold : styleCurrency);
                    } else {
                        String sVal = val != null ? val.toString() : "";
                        if (c == 0 && "ECOLAGE".equals(cat) && !isTotal && r + 2 > startEcolage) {
                            sVal = "";
                        }
                        cell.setCellValue(sVal);

                        CellStyle s = workbook.createCellStyle();
                        s.cloneStyleFrom(isTotal ? styleBold : styleHeader);
                        s.setAlignment(HorizontalAlignment.LEFT);
                        cell.setCellStyle(s);
                    }
                }
            }

            if (startEcolage != -1 && lastEcolage > startEcolage) {
                sheet.addMergedRegion(new CellRangeAddress(startEcolage, lastEcolage, 0, 0));
            }

            for (int i = 0; i < tableModel.getColumnCount(); i++)
                sheet.autoSizeColumn(i);

            workbook.write(fileOut);
            JOptionPane.showMessageDialog(this, "Export réussi !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(fileToSave);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur export : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
