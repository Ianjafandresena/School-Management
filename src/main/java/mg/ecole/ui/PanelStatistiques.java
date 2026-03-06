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
    private final JPanel pnlClasses;
    private final JPanel pnlCategories;

    public PanelStatistiques(MainFrame mainFrame, StatistiquesDAO statsDAO) {
        this.mainFrame = mainFrame;
        this.statsDAO = statsDAO;

        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 22, 16, 22));
        setBackground(new Color(0x1F, 0x23, 0x2C)); // Fond Dashboard

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIFactory.labelTitre("Statistiques Annuelles", "stats.svg"), BorderLayout.WEST);

        JButton btnRefresh = UIFactory.boutonPrincipal("Actualiser");
        btnRefresh.setIcon(UIFactory.icone("refresh.svg", 16));
        btnRefresh.addActionListener(e -> rafraichir());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Contenu ───────────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridLayout(2, 2, 20, 20));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 0, 0, 0));

        pnlEffectifs = creerSectionGraphique("Effectifs par Classe");
        pnlRevenus = creerSectionGraphique("Revenus Mensuels (Écolage)");
        pnlClasses = creerSectionGraphique("Recettes par Classe");
        pnlCategories = creerSectionGraphique("Recettes par Catégorie");

        content.add(pnlEffectifs);
        content.add(pnlRevenus);
        content.add(pnlClasses);
        content.add(pnlCategories);

        add(content, BorderLayout.CENTER);
    }

    private JPanel creerSectionGraphique(String titre) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(new Color(0x2D, 0x32, 0x3B)); // Carte sombre
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x38, 0x3E, 0x48), 1),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel lbl = new JLabel(titre);
        lbl.setFont(Constantes.FONT_BOLD);
        lbl.setForeground(Color.WHITE);
        p.add(lbl, BorderLayout.NORTH);

        JPanel graphContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        graphContainer.setOpaque(false);
        p.add(new JScrollPane(graphContainer) {
            {
                getViewport().setOpaque(false);
                setOpaque(false);
                setBorder(null);
            }
        }, BorderLayout.CENTER);

        return p;
    }

    public void rafraichir() {
        String annee = mainFrame.getAnneeScolaireActuelle();
        try {
            updateGraph(pnlEffectifs, statsDAO.nbElevesParClasse(annee), new Color(0x74, 0xC7, 0xB8));
            updateGraph(pnlRevenus, statsDAO.recettesMensuelles(annee), new Color(0xFF, 0xAE, 0x8F));
            updateGraph(pnlClasses, statsDAO.recettesParClasse(annee), new Color(0x60, 0xA5, 0xFA));
            updatePie(pnlCategories, statsDAO.recettesParType(annee));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur stats : " + e.getMessage());
        }
    }

    private void updateGraph(JPanel section, Map<String, ? extends Number> data, Color color) {
        JScrollPane sp = (JScrollPane) section.getComponent(1);
        JPanel container = (JPanel) sp.getViewport().getView();
        container.removeAll();

        if (data.isEmpty()) {
            JLabel lblEmpty = new JLabel("Aucune donnée disponible");
            lblEmpty.setForeground(Color.GRAY);
            container.add(lblEmpty);
        } else {
            double max = 0;
            for (Number n : data.values())
                if (n.doubleValue() > max)
                    max = n.doubleValue();
            if (max == 0)
                max = 1;

            for (Map.Entry<String, ? extends Number> entry : data.entrySet()) {
                container.add(new BarItem(entry.getKey(), entry.getValue().doubleValue(), max, color));
            }
        }
        container.revalidate();
        container.repaint();
    }

    private void updatePie(JPanel section, Map<String, Double> data) {
        JScrollPane sp = (JScrollPane) section.getComponent(1);
        JPanel container = (JPanel) sp.getViewport().getView();
        container.removeAll();
        if (!data.isEmpty()) {
            container.add(new SimplePieChart(data));
        }
        container.revalidate();
        container.repaint();
    }

    /** Composant graphique interne pour une barre animée. */
    private static class BarItem extends JPanel {
        private final String label;
        private final double valeur;
        private final double max;
        private final Color barColor;
        private double currentHeightFactor = 0;
        private Timer animTimer;

        public BarItem(String label, double valeur, double max, Color barColor) {
            this.label = label;
            this.valeur = valeur;
            this.max = max;
            this.barColor = barColor;
            setPreferredSize(new Dimension(80, 200));
            setOpaque(false);

            animTimer = new Timer(20, e -> {
                currentHeightFactor += 0.05;
                if (currentHeightFactor >= 1.0) {
                    currentHeightFactor = 1.0;
                    animTimer.stop();
                }
                repaint();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int barWidth = 36;
            int maxHeight = h - 60;
            int barHeight = (int) ((valeur / max) * maxHeight * currentHeightFactor);
            if (barHeight < 2 && valeur > 0)
                barHeight = 2;

            // Barre
            g2.setColor(new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 180));
            g2.fillRoundRect((w - barWidth) / 2, h - 35 - barHeight, barWidth, barHeight, 8, 8);
            g2.setColor(barColor);
            g2.drawRoundRect((w - barWidth) / 2, h - 35 - barHeight, barWidth, barHeight, 8, 8);

            // Valeur au dessus
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String valStr = (valeur >= 1000000) ? String.format("%.1fM", valeur / 1000000)
                    : (valeur >= 1000) ? String.format("%.0fk", valeur / 1000) : String.format("%.0f", valeur);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, (w - fm.stringWidth(valStr)) / 2, h - 42 - barHeight);

            // Label en bas
            g2.setColor(new Color(0xD1, 0xD5, 0xDB));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            String shortLabel = label.length() > 10 ? label.substring(0, 8) + "." : label;
            g2.drawString(shortLabel, (w - fm.stringWidth(shortLabel)) / 2, h - 15);

            g2.dispose();
        }
    }

    private static class SimplePieChart extends JPanel {
        private final Map<String, Double> data;
        private static final Color[] COLORS = {
                new Color(0x60, 0xA5, 0xFA), new Color(0x34, 0xD3, 0x99),
                new Color(0xFB, 0xBF, 0x24), new Color(0xF8, 0x71, 0x71),
                new Color(0xA7, 0x8B, 0xFA)
        };

        public SimplePieChart(Map<String, Double> data) {
            this.data = data;
            setPreferredSize(new Dimension(300, 180));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double total = 0;
            for (Double d : data.values())
                total += d;
            if (total == 0)
                total = 1;

            int x = 10, y = 10, diam = 160;
            int startAngle = 0;
            int i = 0;

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int arcAngle = (int) Math.round((entry.getValue() / total) * 360);
                g2.setColor(COLORS[i % COLORS.length]);
                g2.fillArc(x, y, diam, diam, startAngle, arcAngle);

                g2.fillRect(x + diam + 20, y + (i * 25), 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String label = String.format("%s (%.0f%%)", entry.getKey(), (entry.getValue() / total) * 100);
                g2.drawString(label, x + diam + 40, y + (i * 25) + 12);

                startAngle += arcAngle;
                i++;
            }
            g2.dispose();
        }
    }
}
