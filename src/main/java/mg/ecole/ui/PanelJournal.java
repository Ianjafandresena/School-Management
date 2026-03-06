package mg.ecole.ui;

import mg.ecole.dao.JournalDAO;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel d'affichage des logs système (Journal).
 */
public class PanelJournal extends JPanel {

    private final JournalDAO journalDAO;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public PanelJournal(JournalDAO journalDAO) {
        this.journalDAO = journalDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 22, 16, 22));

        // En-tête
        add(UIFactory.labelTitre("📜 Journal des Actions"), BorderLayout.NORTH);

        // Tableau
        tableModel = new DefaultTableModel(new String[] {
                "Date", "Utilisateur", "Action", "Détails"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = UIFactory.creerTable();
        table.setModel(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void rafraichir() {
        try {
            tableModel.setRowCount(0);
            List<String[]> logs = journalDAO.listerDernieresActions(200);
            for (String[] l : logs)
                tableModel.addRow(l);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur logs : " + e.getMessage());
        }
    }
}
