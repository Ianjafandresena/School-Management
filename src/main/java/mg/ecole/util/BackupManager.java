package mg.ecole.util;

import mg.ecole.database.DatabaseManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilitaire pour la sauvegarde et la restauration de la base de données
 * SQLite.
 */
public class BackupManager {

    public static String sauvegarder(String dossierDest) throws IOException {
        File source = new File(DatabaseManager.getDbPath());
        if (!source.exists())
            throw new IOException("Base de données introuvable.");

        File dir = new File(dossierDest);
        if (!dir.exists())
            dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File dest = new File(dir, "backup_ecole_" + timestamp + ".db");

        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getAbsolutePath();
    }

    public static void restaurer(File fichierBackup) throws IOException {
        File dest = new File(DatabaseManager.getDbPath());
        // Fermer la connexion avant de restaurer
        DatabaseManager.getInstance().closeConnection();

        Files.copy(fichierBackup.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // La connexion sera réouverte au prochain appel
    }
}
