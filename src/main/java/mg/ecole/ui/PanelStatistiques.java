package mg.ecole.ui;

import mg.ecole.dao.StatistiquesDAO;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

/**
 * Panel de visualisation des statistiques.
 * Affiche des graphiques simples (barres) pour les revenus et les effectifs.
 */
public class PanelStatistiques extends JPanel {

    private final MainFrame mainFrame;
    private final StatistiquesDAO statsDAO;

    private final JPanel pnlEffectifs;
    private final JPanel pnlRevenus;

    public PanelStatistiques(MainFrame mainFrame, StatistiquesDAO statsDAO) {
        this.mainFrame = mainFrame;
        this.statsDAO = statsDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 22, 16, 22));

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIFactory.labelTitre("📊 Statistiques Annuelles"), BorderLayout.WEST);

        JButton btnRefresh = UIFactory.boutonPrincipal("🔄 Actualiser");
        btnRefresh.addActionListener(e -> rafraichir());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Contenu ───────────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 20));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 0, 0, 0));

        pnlEffectifs = creerSectionGraphique("👨‍🎓 Effectifs par Classe");
        pnlRevenus = creerSectionGraphique("💰 Revenus Mensuels (Écolage)");

        content.add(pnlEffectifs);
        content.add(pnlRevenus);

        add(content, BorderLayout.CENTER);
    }

    private JPanel creerSectionGraphique(String titre) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 240), 1),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel lbl = new JLabel(titre);
        lbl.setFont(Constantes.FONT_BOLD);
        p.add(lbl, BorderLayout.NORTH);

        JPanel graphContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        graphContainer.setOpaque(false);
        p.add(new JScrollPane(graphContainer), BorderLayout.CENTER);

        return p;
    }

    public void rafraichir() {
        String annee = mainFrame.getAnneeScolaireActuelle();
        try {
            updateGraph(pnlEffectifs, statsDAO.nbElevesParClasse(annee), "élèves", new Color(0x74, 0xC7, 0xB8));
            updateGraph(pnlRevenus, statsDAO.recettesMensuelles(annee), "Ar", new Color(0xFF, 0xAE, 0x8F));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur stats : " + e.getMessage());
        }
    }

    private void updateGraph(JPanel section, Map<String, ? extends Number> data, String unite, Color color) {
        JScrollPane sp = (JScrollPane) section.getComponent(1);
        JPanel container = (JPanel) sp.getViewport().getView();
        container.removeAll();

        if (data.isEmpty()) {
            container.add(new JLabel("Aucune donnée disponible pour cette année."));
        } else {
            // Trouver le max pour l'échelle
            double max = 0;
            for (Number n : data.values())
                if (n.doubleValue() > max)
                    max = n.doubleValue();
            if (max == 0)
                max = 1;

            for (Map.Entry<String, ? extends Number> entry : data.entrySet()) {
                container.add(new BarItem(entry.getKey(), entry.getValue().doubleValue(), max, unite, color));
            }
        }
        container.revalidate();
        container.repaint();
    }

    /** Composant graphique interne pour une barre. */
    private static class BarItem extends JPanel {
        private final String label;
        private final double valeur;
        private final double max;
        private final String unite;
        private final Color barColor;

        public BarItem(String label, double valeur, double max, String unite, Color barColor) {
            this.label = label;
            this.valeur = valeur;
            this.max = max;
            this.unite = unite;
            this.barColor = barColor;
            setPreferredSize(new Dimension(80, 200));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int barWidth = 34;
            int maxHeight = h - 60;
            int barHeight = (int) ((valeur / max) * maxHeight);
            if (barHeight < 2 && valeur > 0)
                barHeight = 2;

            // Dessiner la barre
            g2.setColor(barColor);
            g2.fillRoundRect((w - barWidth) / 2, h - 35 - barHeight, barWidth, barHeight, 6, 6);

            // Valeur au dessus
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String valStr = (valeur > 1000) ? String.format("%.0fk", valeur / 1000) : String.format("%.0f", valeur);
            if ("Ar".equals(unite))
                valStr += "";
            else
                valStr += " " + unite;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, (w - fm.stringWidth(valStr)) / 2, h - 40 - barHeight);

            // Label en bas
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            String shortLabel = label.length() > 10 ? label.substring(0, 8) + ".." : label;
            g2.drawString(shortLabel, (w - fm.stringWidth(shortLabel)) / 2, h - 15);

            g2.dispose();
        }
    }
}
