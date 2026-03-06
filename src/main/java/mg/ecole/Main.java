package mg.ecole;

import com.formdev.flatlaf.FlatDarkLaf;
import mg.ecole.database.DatabaseInitializer;
import mg.ecole.dao.UtilisateurDAO;
import mg.ecole.ui.DialogLogin;
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

        // 2. Afficher Splash Screen (facultatif ici pour simplifier)
        System.out.println("Base de données initialisée.");

        // 3. Authentification
        SwingUtilities.invokeLater(() -> {
            UtilisateurDAO userDAO = new UtilisateurDAO();
            DialogLogin loginDlg = new DialogLogin(null, userDAO);
            loginDlg.setVisible(true);

            if (loginDlg.isSuccess()) {
                // 4. Lancer l'application principale si auth réussie
                MainFrame frame = new MainFrame();
                frame.setUserSession(loginDlg.getUserLogin(), loginDlg.getUserRole());
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
