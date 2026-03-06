package mg.ecole.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

/**
 * Gestionnaire de connexion SQLite - Singleton.
 * La base de données est stockée dans %APPDATA%\EcolePrimaire\ecole.db
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final String DB_FOLDER = System.getenv("APPDATA") + File.separator + "EcolePrimaire";
    private static final String DB_PATH = DB_FOLDER + File.separator + "ecole.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    private Connection connection;

    private DatabaseManager() {
        // Créer le dossier si nécessaire
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Retourne l'instance unique du gestionnaire de connexion.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Ouvre (ou retourne) la connexion à la base de données SQLite.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite introuvable.", e);
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Activer les clés étrangères
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode = WAL;");
            }
        }
        return connection;
    }

    /**
     * Ferme proprement la connexion.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne le chemin complet vers le fichier de base de données.
     */
    public static String getDbPath() {
        return DB_PATH;
    }
}
