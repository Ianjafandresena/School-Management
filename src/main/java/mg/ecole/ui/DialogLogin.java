package mg.ecole.ui;

import mg.ecole.dao.UtilisateurDAO;
import mg.ecole.util.Constantes;
import mg.ecole.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Boîte de dialogue de connexion redessinée avec un style premium.
 */
public class DialogLogin extends JDialog {

    private final UtilisateurDAO dao;
    private final JTextField txtLogin;
    private final JPasswordField txtPass;
    private boolean success = false;
    private String userRole = "";
    private String userLogin = "";

    public DialogLogin(Frame owner, UtilisateurDAO dao) {
        super(owner, "🔐 Accès Sécurisé - " + Constantes.NOM_APP, true);
        this.dao = dao;

        setUndecorated(true); // Pour un look plus pur, ou garder si on veut la barre de titre
        // Finalement on garde la barre pour pouvoir fermer facilement, mais on stylise
        // le contenu.
        setUndecorated(false);

        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Panneau principal avec fond sombre
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0x24, 0x2B, 0x3E)); // Bleu très sombre comme l'image
        mainPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. Logo WavOlution
        ImageIcon logoIcon = UIFactory.iconeImage("wavolution.png", 180); // Un peu plus grand
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 25, 0);
            mainPanel.add(lblLogo, gbc);
        }

        // 2. Titre Application
        JLabel lblTitreApp = new JLabel(Constantes.NOM_APP);
        lblTitreApp.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitreApp.setForeground(Color.WHITE);
        lblTitreApp.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(lblTitreApp, gbc);

        // 3. Sous-titre
        JLabel lblSousTitre = new JLabel("Système de gestion scolaire");
        lblSousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSousTitre.setForeground(new Color(0xAA, 0xB2, 0xC0));
        lblSousTitre.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 40, 0);
        mainPanel.add(lblSousTitre, gbc);

        // 4. Carte de connexion (le rectangle bleu foncé)
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(0x2E, 0x38, 0x4D)); // Légèrement plus clair que le fond
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3E, 0x48, 0x5D), 1),
                new EmptyBorder(30, 35, 30, 35)));

        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.fill = GridBagConstraints.HORIZONTAL;
        gbcCard.gridx = 0;
        gbcCard.weightx = 1.0;

        // Label Utilisateur
        JLabel lblUser = new JLabel("Nom d'utilisateur");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(Color.WHITE);
        gbcCard.gridy = 0;
        gbcCard.insets = new Insets(0, 0, 10, 0);
        card.add(lblUser, gbcCard);

        // Champ Utilisateur
        txtLogin = new JTextField();
        txtLogin.setPreferredSize(new Dimension(350, 45));
        txtLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLogin.setBackground(new Color(0x38, 0x44, 0x59));
        txtLogin.setForeground(Color.WHITE);
        txtLogin.setCaretColor(Color.WHITE);
        txtLogin.putClientProperty("JTextField.placeholderText", "Entrez votre nom d'utilisateur");
        txtLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4E, 0x58, 0x6D)),
                new EmptyBorder(0, 15, 0, 15)));
        gbcCard.gridy = 1;
        gbcCard.insets = new Insets(0, 0, 25, 0);
        card.add(txtLogin, gbcCard);

        // Label Mot de passe
        JLabel lblPass = new JLabel("Mot de passe");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(Color.WHITE);
        gbcCard.gridy = 2;
        gbcCard.insets = new Insets(0, 0, 10, 0);
        card.add(lblPass, gbcCard);

        // Champ Mot de passe
        txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(350, 45));
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBackground(new Color(0x38, 0x44, 0x59));
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(Color.WHITE);
        txtPass.putClientProperty("JTextField.placeholderText", "Entrez votre mot de passe");
        txtPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4E, 0x58, 0x6D)),
                new EmptyBorder(0, 15, 0, 15)));
        gbcCard.gridy = 3;
        gbcCard.insets = new Insets(0, 0, 20, 0);
        card.add(txtPass, gbcCard);

        // Ligne Options (Checkbox + Link)
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);

        JCheckBox chkRemember = new JCheckBox("Se souvenir de moi");
        chkRemember.setForeground(new Color(0xAA, 0xB2, 0xC0));
        chkRemember.setOpaque(false);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        optionsPanel.add(chkRemember, BorderLayout.WEST);

        JLabel lblForgot = new JLabel("Mot de passe oublié ?");
        lblForgot.setForeground(new Color(0x34, 0x98, 0xDB));
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        optionsPanel.add(lblForgot, BorderLayout.EAST);

        gbcCard.gridy = 4;
        gbcCard.insets = new Insets(0, 0, 30, 0);
        card.add(optionsPanel, gbcCard);

        // Bouton Connexion
        JButton btnLogin = new JButton("Se connecter") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1E, 0x90, 0xFF)); // Bleu vif
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(350, 50));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(this::onLogin);
        gbcCard.gridy = 5;
        gbcCard.insets = new Insets(0, 0, 10, 0);
        card.add(btnLogin, gbcCard);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 40, 0);
        mainPanel.add(card, gbc);

        // 5. Footer Copyright
        JPanel footer = new JPanel(new GridBagLayout());
        footer.setOpaque(false);
        GridBagConstraints gbcFooter = new GridBagConstraints();
        gbcFooter.gridx = 0;

        JLabel lblDevBy = new JLabel("© 2026 - Développé par WavOlution");
        lblDevBy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDevBy.setForeground(new Color(0x7F, 0x8C, 0x8D));
        lblDevBy.setHorizontalAlignment(SwingConstants.CENTER);
        gbcFooter.gridy = 0;
        footer.add(lblDevBy, gbcFooter);

        JLabel lblReserved = new JLabel("Tous droits réservés");
        lblReserved.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblReserved.setForeground(new Color(0x7F, 0x8C, 0x8D));
        lblReserved.setHorizontalAlignment(SwingConstants.CENTER);
        gbcFooter.gridy = 1;
        gbcFooter.insets = new Insets(3, 0, 0, 0);
        footer.add(lblReserved, gbcFooter);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(footer, gbc);

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);

        // Initialiser l'admin si nécessaire
        try {
            dao.creerUtilisateurInitial();
        } catch (Exception ignore) {
        }
    }

    private void onLogin(ActionEvent e) {
        String login = txtLogin.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (login.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Attention",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        dao.authentifier(login, pass, new UtilisateurDAO.AuthResult() {
            @Override
            public void onSuccess(String l, String r) {
                success = true;
                userLogin = l;
                userRole = r;
                dispose();
            }

            @Override
            public void onFailure(String message) {
                JOptionPane.showMessageDialog(DialogLogin.this, message, "Erreur de connexion",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getUserLogin() {
        return userLogin;
    }
}
