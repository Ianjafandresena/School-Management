package mg.ecole;

import com.formdev.flatlaf.FlatDarkLaf;
import mg.ecole.database.DatabaseInitializer;
import mg.ecole.dao.UtilisateurDAO;
import mg.ecole.ui.DialogLogin;
import mg.ecole.ui.DialogSetup;
import mg.ecole.ui.MainFrame;

import javax.swing.*;

/**
 * Point d'entrée de l'application.
 */
public class Main {
    public static void main(String[] args) {
        // Appliquer le thème FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1. Initialiser la base de données
        DatabaseInitializer.initialiserSiNecessaire();
        System.out.println("Base de données initialisée.");

        // 2. Authentification
        SwingUtilities.invokeLater(() -> {
            UtilisateurDAO userDAO = new UtilisateurDAO();

            // Vérifier si c'est le premier lancement
            boolean firstLaunch = false;
            try {
                firstLaunch = userDAO.isFirstLaunch();
            } catch (Exception ignore) {
            }

            if (firstLaunch) {
                // Afficher l'assistant de configuration
                DialogSetup setupDlg = new DialogSetup(null, userDAO);
                setupDlg.setVisible(true);

                if (setupDlg.isCompleted()) {
                    // L'utilisateur a créé son compte, le connecter directement
                    MainFrame frame = new MainFrame();
                    frame.setUserSession(setupDlg.getCreatedLogin(), setupDlg.getCreatedRole());
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            } else {
                // Lancement normal avec login
                DialogLogin loginDlg = new DialogLogin(null, userDAO);
                loginDlg.setVisible(true);

                if (loginDlg.isSuccess()) {
                    MainFrame frame = new MainFrame();
                    frame.setUserSession(loginDlg.getUserLogin(), loginDlg.getUserRole());
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        });
    }
}
