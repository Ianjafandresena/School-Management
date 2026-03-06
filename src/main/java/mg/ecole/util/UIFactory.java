package mg.ecole.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Fabrique de composants Swing stylisés pour l'interface.
 */
public class UIFactory {

    private UIFactory() {
    }

    // ─── Boutons ──────────────────────────────────────────────────────────────

    /** Bouton principal (bleu). */
    public static JButton boutonPrincipal(String texte) {
        return creerBouton(texte, Constantes.COULEUR_PRIMAIRE, Color.WHITE);
    }

    /** Bouton succès (vert). */
    public static JButton boutonSucces(String texte) {
        return creerBouton(texte, Constantes.COULEUR_SECONDAIRE, Color.WHITE);
    }

    /** Bouton danger (rouge). */
    public static JButton boutonDanger(String texte) {
        return creerBouton(texte, Constantes.COULEUR_DANGER, Color.WHITE);
    }

    /** Bouton secondaire (gris transparent). */
    public static JButton boutonSecondaire(String texte) {
        JButton btn = new JButton(texte);
        btn.setFont(Constantes.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(Constantes.TAILLE_BOUTON);
        return btn;
    }

    private static JButton creerBouton(String texte, Color fond, Color texteColor) {
        JButton btn = new JButton(texte) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Constantes.FONT_BOLD);
        btn.setBackground(fond);
        btn.setForeground(texteColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(Constantes.TAILLE_BOUTON);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(fond.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(fond);
            }
        });
        return btn;
    }

    // ─── Labels ───────────────────────────────────────────────────────────────

    public static JLabel labelTitre(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(Constantes.FONT_TITRE);
        return lbl;
    }

    /** Crée un label de titre avec une icône SVG. */
    public static JLabel labelTitre(String texte, String iconName) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(Constantes.FONT_TITRE);
        lbl.setIcon(icone(iconName, 28));
        lbl.setIconTextGap(12);
        return lbl;
    }

    public static JLabel labelSousTitre(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(Constantes.FONT_SOUS_TITRE);
        return lbl;
    }

    public static JLabel label(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(Constantes.FONT_NORMALE);
        return lbl;
    }

    // ─── Champs de saisie ─────────────────────────────────────────────────────

    public static JTextField champTexte(int colonnes) {
        JTextField field = new JTextField(colonnes);
        field.setFont(Constantes.FONT_NORMALE);
        return field;
    }

    public static JTextField champTexte() {
        return champTexte(20);
    }

    public static JComboBox<String> comboBox(String[] valeurs) {
        JComboBox<String> combo = new JComboBox<>(valeurs);
        combo.setFont(Constantes.FONT_NORMALE);
        return combo;
    }

    /**
     * Crée un sélecteur de date moderne (LGoodDatePicker) configuré en Français.
     */
    public static DatePicker datePicker() {
        DatePickerSettings settings = new DatePickerSettings(Locale.FRENCH);
        settings.setFormatForDatesCommonEra("dd/MM/yyyy");
        settings.setFormatForDatesBeforeCommonEra("dd/MM/yyyy");
        settings.setFontValidDate(Constantes.FONT_NORMALE);

        DatePicker picker = new DatePicker(settings);
        picker.setFont(Constantes.FONT_NORMALE);
        // Style FlatLaf dark compatible
        picker.getComponentDateTextField().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return picker;
    }

    /**
     * Crée une icône SVG redimensionnée (FlatLaf Extras).
     */
    public static FlatSVGIcon icone(String relativePath, int size) {
        FlatSVGIcon icon = new FlatSVGIcon("icons/" + relativePath);
        return icon.derive(size, size);
    }

    /**
     * Charge une image PNG/JPG et la redimensionne.
     */
    public static ImageIcon iconeImage(String relativePath, int size) {
        try {
            java.net.URL url = UIFactory.class.getResource("/icons/" + relativePath);
            if (url == null)
                return null;
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Tableaux ─────────────────────────────────────────────────────────────

    /**
     * Crée un JTable non éditable, avec tri activé et sélection de ligne entière.
     */
    public static JTable creerTable() {
        JTable table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table.setFont(Constantes.FONT_NORMALE);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Constantes.FONT_BOLD);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return table;
    }

    // ─── Panneaux ─────────────────────────────────────────────────────────────

    /** Panneau avec bordure vide (marges). */
    public static JPanel panelAvecMarge(int marge) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(marge, marge, marge, marge));
        return panel;
    }

    // ─── Badge statistique pour le tableau de bord ────────────────────────────

    public static JPanel badgeStat(String titre, String valeur, Color couleur) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(couleur);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setPreferredSize(new Dimension(200, 110));

        JLabel lblValeur = new JLabel(valeur, SwingConstants.CENTER);
        lblValeur.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValeur.setForeground(couleur);

        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(Constantes.FONT_BOLD);

        card.add(lblValeur, BorderLayout.CENTER);
        card.add(lblTitre, BorderLayout.SOUTH);
        return card;
    }
}
