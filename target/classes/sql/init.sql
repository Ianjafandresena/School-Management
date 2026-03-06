-- ============================================================
-- Script d'initialisation de la base de données
-- Ecole Primaire - Tables en français sans accents
-- ============================================================

-- Table des parametres de l'ecole
CREATE TABLE IF NOT EXISTS parametres (
    cle   TEXT PRIMARY KEY,
    valeur TEXT NOT NULL
);

INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('nom_ecole', 'Ecole Primaire');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('adresse_ecole', '');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('telephone_ecole', '');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('annee_scolaire', '2025-2026');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('slogan', '');

-- Table des utilisateurs (authentification)
CREATE TABLE IF NOT EXISTS utilisateurs (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nom          TEXT NOT NULL,
    login        TEXT NOT NULL UNIQUE,
    mot_de_passe TEXT NOT NULL,
    role         TEXT NOT NULL DEFAULT 'SECRETAIRE',
    actif        INTEGER NOT NULL DEFAULT 1,
    date_creation TEXT NOT NULL
);

INSERT OR IGNORE INTO utilisateurs (nom, login, mot_de_passe, role, actif, date_creation)
VALUES ('Administrateur', 'admin', 'admin123', 'ADMIN', 1, date('now'));

-- Table des niveaux scolaires
CREATE TABLE IF NOT EXISTS niveaux (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    code        TEXT NOT NULL UNIQUE,
    libelle     TEXT NOT NULL,
    ordre       INTEGER NOT NULL DEFAULT 0,
    description TEXT
);

INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('TPS', 'Toute Petite Section', 1);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('PS',  'Petite Section', 2);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('MS',  'Moyenne Section', 3);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('GS',  'Grande Section', 4);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('CP',  'Cours Préparatoire', 5);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('CE1', 'Cours Elémentaire 1', 6);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('CE2', 'Cours Elémentaire 2', 7);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('CM1', 'Cours Moyen 1', 8);
INSERT OR IGNORE INTO niveaux (code, libelle, ordre) VALUES ('CM2', 'Cours Moyen 2', 9);

-- Table des enseignants
CREATE TABLE IF NOT EXISTS enseignants (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    matricule       TEXT NOT NULL UNIQUE,
    nom             TEXT NOT NULL,
    prenom          TEXT NOT NULL,
    sexe            TEXT NOT NULL DEFAULT 'M',
    telephone       TEXT,
    email           TEXT,
    matiere         TEXT,
    date_embauche   TEXT,
    actif           INTEGER NOT NULL DEFAULT 1
);

-- Table des classes
CREATE TABLE IF NOT EXISTS classes (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nom             TEXT NOT NULL,
    niveau_id       INTEGER NOT NULL,
    annee_scolaire  TEXT NOT NULL,
    enseignant_id   INTEGER,
    capacite_max    INTEGER NOT NULL DEFAULT 30,
    description     TEXT,
    FOREIGN KEY (niveau_id) REFERENCES niveaux(id),
    FOREIGN KEY (enseignant_id) REFERENCES enseignants(id),
    UNIQUE (nom, annee_scolaire)
);

-- Table des eleves
CREATE TABLE IF NOT EXISTS eleves (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    matricule       TEXT NOT NULL UNIQUE,
    nom             TEXT NOT NULL,
    prenom          TEXT NOT NULL,
    date_naissance  TEXT,
    lieu_naissance  TEXT,
    sexe            TEXT NOT NULL DEFAULT 'M',
    adresse         TEXT,
    nom_parent      TEXT,
    telephone_parent TEXT,
    lien_parent     TEXT DEFAULT 'Père',
    photo           TEXT,
    date_creation   TEXT NOT NULL
);

-- Table des inscriptions (lien élève ↔ classe ↔ année scolaire)
CREATE TABLE IF NOT EXISTS inscriptions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    eleve_id        INTEGER NOT NULL,
    classe_id       INTEGER NOT NULL,
    annee_scolaire  TEXT NOT NULL,
    date_inscription TEXT NOT NULL,
    statut          TEXT NOT NULL DEFAULT 'ACTIF',
    FOREIGN KEY (eleve_id) REFERENCES eleves(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id),
    UNIQUE (eleve_id, annee_scolaire)
);
