package mg.ecole.ui;

import mg.ecole.dao.UtilisateurDAO;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Assistant de configuration du premier lancement.
 * Demande à l'utilisateur de créer son compte principal.
 * L'utilisateur de secours admin/admin123 reste caché.
 */
public class DialogSetup extends JDialog {

    private boolean completed = false;
    private String createdLogin = "";
    private String createdRole = "";

    public DialogSetup(Frame owner, UtilisateurDAO dao) {
        super(owner, "Configuration Initiale - " + Constantes.NOM_APP, true);
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Panneau principal avec fond sombre
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0x24, 0x2B, 0x3E));
        mainPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Logo
        ImageIcon logoIcon = UIFactory.iconeImage("wavolution.png", 120);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 15, 0);
            mainPanel.add(lblLogo, gbc);
        }

        // Titre
        JLabel lblTitre = new JLabel("Bienvenue !");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(lblTitre, gbc);

        // Sous-titre
        JLabel lblSousTitre = new JLabel("Configurez votre compte administrateur");
        lblSousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSousTitre.setForeground(new Color(0xAA, 0xB2, 0xC0));
        lblSousTitre.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        mainPanel.add(lblSousTitre, gbc);

        // Carte de formulaire
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(0x2E, 0x38, 0x4D));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3E, 0x48, 0x5D), 1),
                new EmptyBorder(25, 30, 25, 30)));

        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.fill = GridBagConstraints.HORIZONTAL;
        gbcCard.gridx = 0;
        gbcCard.gridwidth = 2;
        gbcCard.weightx = 1.0;

        // Champ Nom complet
        JLabel lblNom = new JLabel("Nom complet");
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNom.setForeground(Color.WHITE);
        gbcCard.gridy = 0;
        gbcCard.insets = new Insets(0, 0, 6, 0);
        card.add(lblNom, gbcCard);

        JTextField txtNom = createStyledField();
        txtNom.putClientProperty("JTextField.placeholderText", "Ex: Jean RAKOTO");
        gbcCard.gridy = 1;
        gbcCard.insets = new Insets(0, 0, 18, 0);
        card.add(txtNom, gbcCard);

        // Champ Login
        JLabel lblLogin = new JLabel("Identifiant de connexion");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLogin.setForeground(Color.WHITE);
        gbcCard.gridy = 2;
        gbcCard.insets = new Insets(0, 0, 6, 0);
        card.add(lblLogin, gbcCard);

        JTextField txtLogin = createStyledField();
        txtLogin.putClientProperty("JTextField.placeholderText", "Ex: jrakoto");
        gbcCard.gridy = 3;
        gbcCard.insets = new Insets(0, 0, 18, 0);
        card.add(txtLogin, gbcCard);

        // Champ Mot de passe
        JLabel lblMdp = new JLabel("Mot de passe");
        lblMdp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMdp.setForeground(Color.WHITE);
        gbcCard.gridy = 4;
        gbcCard.insets = new Insets(0, 0, 6, 0);
        card.add(lblMdp, gbcCard);

        JPasswordField txtMdp = createStyledPasswordField();
        txtMdp.putClientProperty("JTextField.placeholderText", "Minimum 4 caractères");
        gbcCard.gridy = 5;
        gbcCard.insets = new Insets(0, 0, 18, 0);
        card.add(txtMdp, gbcCard);

        // Champ Confirmation
        JLabel lblConfirm = new JLabel("Confirmer le mot de passe");
        lblConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblConfirm.setForeground(Color.WHITE);
        gbcCard.gridy = 6;
        gbcCard.insets = new Insets(0, 0, 6, 0);
        card.add(lblConfirm, gbcCard);

        JPasswordField txtConfirm = createStyledPasswordField();
        txtConfirm.putClientProperty("JTextField.placeholderText", "Retapez le mot de passe");
        gbcCard.gridy = 7;
        gbcCard.insets = new Insets(0, 0, 20, 0);
        card.add(txtConfirm, gbcCard);

        // Avertissement
        JLabel lblWarning = new JLabel(
                "<html><center>⚠ Veuillez noter ces informations dans un endroit sûr.<br>Elles seront nécessaires pour vous connecter.</center></html>");
        lblWarning.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblWarning.setForeground(new Color(0xFB, 0xBF, 0x24));
        lblWarning.setHorizontalAlignment(SwingConstants.CENTER);
        gbcCard.gridy = 8;
        gbcCard.insets = new Insets(0, 0, 20, 0);
        card.add(lblWarning, gbcCard);

        // Bouton Créer
        JButton btnCreer = new JButton("Créer mon compte") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x10, 0xB9, 0x81));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCreer.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCreer.setForeground(Color.WHITE);
        btnCreer.setPreferredSize(new Dimension(350, 48));
        btnCreer.setContentAreaFilled(false);
        btnCreer.setBorderPainted(false);
        btnCreer.setFocusPainted(false);
        btnCreer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreer.addActionListener(e -> {
            String nom = txtNom.getText().trim();
            String login = txtLogin.getText().trim();
            String mdp = new String(txtMdp.getPassword());
            String confirm = new String(txtConfirm.getPassword());

            if (nom.isEmpty() || login.isEmpty() || mdp.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tous les champs sont obligatoires.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (login.length() < 3) {
                JOptionPane.showMessageDialog(this,
                        "L'identifiant doit contenir au moins 3 caractères.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (mdp.length() < 4) {
                JOptionPane.showMessageDialog(this,
                        "Le mot de passe doit contenir au moins 4 caractères.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!mdp.equals(confirm)) {
                JOptionPane.showMessageDialog(this,
                        "Les mots de passe ne correspondent pas.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("admin".equalsIgnoreCase(login)) {
                JOptionPane.showMessageDialog(this,
                        "Cet identifiant est réservé. Veuillez en choisir un autre.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (dao.loginExiste(login)) {
                    JOptionPane.showMessageDialog(this,
                            "Cet identifiant existe déjà.", "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dao.creerUtilisateur(nom, login, mdp, "ADMIN");
                completed = true;
                createdLogin = login;
                createdRole = "ADMIN";

                JOptionPane.showMessageDialog(this,
                        "<html><b>Compte créé avec succès !</b><br><br>" +
                                "Identifiant : <b>" + login + "</b><br>" +
                                "N'oubliez pas de noter votre mot de passe.<br><br>" +
                                "Vous allez maintenant être connecté.</html>",
                        "Configuration terminée",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la création : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbcCard.gridy = 9;
        gbcCard.insets = new Insets(0, 0, 0, 0);
        card.add(btnCreer, gbcCard);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(card, gbc);

        // Footer
        JLabel lblFooter = new JLabel("© 2026 - Développé par WavOlution");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(new Color(0x7F, 0x8C, 0x8D));
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(lblFooter, gbc);

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(320, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(0x38, 0x44, 0x59));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4E, 0x58, 0x6D)),
                new EmptyBorder(0, 12, 0, 12)));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(320, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(0x38, 0x44, 0x59));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4E, 0x58, 0x6D)),
                new EmptyBorder(0, 12, 0, 12)));
        return field;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getCreatedLogin() {
        return createdLogin;
    }

    public String getCreatedRole() {
        return createdRole;
    }
}
