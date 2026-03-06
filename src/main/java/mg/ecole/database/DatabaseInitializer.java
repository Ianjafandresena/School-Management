package mg.ecole.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initialise automatiquement la base de données au premier lancement.
 * Exécute init.sql pour les tables de base + migration_v2.sql pour les modules
 * avancés.
 * Toutes les instructions utilisent IF NOT EXISTS — sans risque d'écraser les
 * données.
 */
public class DatabaseInitializer {

    public static void initialiserSiNecessaire() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            // Toujours exécuter init.sql (IF NOT EXISTS protège les données)
            executerScript(conn, "/sql/init.sql");
            // Toujours exécuter migration_v2.sql (modules paiements, journal, etc.)
            executerScript(conn, "/sql/migration_v2.sql");
            // Toujours exécuter migration_v3.sql (matières, multi-matières)
            executerScript(conn, "/sql/migration_v3.sql");
            // Migration v4: Nouveaux champs parents pour les élèves
            executerScript(conn, "/sql/migration_v4.sql");
            // Migration v5: Frais scolaires par niveau
            executerScript(conn, "/sql/migration_v5.sql");
            System.out.println("[DB] Base de données prête.");
        } catch (SQLException | IOException e) {
            System.err.println("[DB] Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executerScript(Connection conn, String cheminRessource) throws IOException, SQLException {
        InputStream is = DatabaseInitializer.class.getResourceAsStream(cheminRessource);
        if (is == null) {
            System.err.println("[DB] Script non trouvé : " + cheminRessource);
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                String trimmed = ligne.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty())
                    continue;
                sb.append(ligne).append("\n");
            }
        }

        // Exécuter chaque instruction SQL séparément
        String[] instructions = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String sql : instructions) {
                String s = sql.trim();
                if (!s.isEmpty()) {
                    try {
                        stmt.execute(s);
                    } catch (SQLException ex) {
                        // Ignorer les erreurs de duplication sur INSERT OR IGNORE
                        if (!ex.getMessage().contains("UNIQUE constraint failed")) {
                            System.err.println("[DB] Avertissement SQL: " + ex.getMessage());
                        }
                    }
                }
            }
        }
        System.out.println("[DB] Script exécuté : " + cheminRessource);
    }
}
