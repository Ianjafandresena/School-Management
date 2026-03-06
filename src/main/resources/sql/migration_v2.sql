-- ============================================================
-- Migration v2 : Paiements, Années scolaires, Utilisateurs, Journal
-- Exécutée automatiquement par DatabaseInitializer si les tables n'existent pas
-- ============================================================

-- Table des annees scolaires
CREATE TABLE IF NOT EXISTS annees_scolaires (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    libelle   TEXT NOT NULL UNIQUE,         -- ex: "2025-2026"
    date_debut TEXT,
    date_fin  TEXT,
    active    INTEGER NOT NULL DEFAULT 0,   -- 1 = année en cours
    archivee  INTEGER NOT NULL DEFAULT 0
);

INSERT OR IGNORE INTO annees_scolaires (libelle, active) VALUES ('2025-2026', 1);

-- Table des types de paiement
CREATE TABLE IF NOT EXISTS types_paiement (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    code    TEXT NOT NULL UNIQUE,
    libelle TEXT NOT NULL,
    est_mensuel INTEGER NOT NULL DEFAULT 0  -- 1 = paiement par mois (ECOLAGE), 0 = forfait
);

INSERT OR IGNORE INTO types_paiement (code, libelle, est_mensuel) VALUES ('DROITS',        'Droits',        0);
INSERT OR IGNORE INTO types_paiement (code, libelle, est_mensuel) VALUES ('ECOLAGE',       'Ecolage',       1);
INSERT OR IGNORE INTO types_paiement (code, libelle, est_mensuel) VALUES ('FRAM',          'FRAM',          0);
INSERT OR IGNORE INTO types_paiement (code, libelle, est_mensuel) VALUES ('PARTICIPATION', 'Participation', 0);

-- Table des paiements
-- Un paiement lie un élève à un type, une année scolaire, et optionnellement un mois (pour ECOLAGE)
CREATE TABLE IF NOT EXISTS paiements (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    eleve_id        INTEGER NOT NULL,
    classe_id       INTEGER NOT NULL,
    annee_scolaire  TEXT NOT NULL,
    type_paiement   TEXT NOT NULL,          -- code: DROITS, ECOLAGE, FRAM, PARTICIPATION
    mois            TEXT,                   -- NULL si non mensuel, sinon: SEPTEMBRE, OCTOBRE...
    montant         REAL NOT NULL DEFAULT 0,
    date_paiement   TEXT NOT NULL,
    mode_paiement   TEXT DEFAULT 'ESPECES', -- ESPECES, CHEQUE, VIREMENT, MOBILE
    reference       TEXT,                   -- numéro de reçu ou référence
    note            TEXT,
    utilisateur_id  INTEGER,
    FOREIGN KEY (eleve_id)  REFERENCES eleves(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id)
);

-- Index pour performances sur les requêtes comptabilité
CREATE INDEX IF NOT EXISTS idx_paiements_annee   ON paiements(annee_scolaire);
CREATE INDEX IF NOT EXISTS idx_paiements_classe  ON paiements(classe_id);
CREATE INDEX IF NOT EXISTS idx_paiements_eleve   ON paiements(eleve_id);
CREATE INDEX IF NOT EXISTS idx_paiements_type    ON paiements(type_paiement);

-- Table des utilisateurs (version complète avec roles)
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

-- Table du journal des actions
CREATE TABLE IF NOT EXISTS journal (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    utilisateur   TEXT NOT NULL,
    action        TEXT NOT NULL,
    details       TEXT,
    date_action   TEXT NOT NULL
);

-- Table des parametres (s'assure qu'elle existe avec les bons champs)
CREATE TABLE IF NOT EXISTS parametres (
    cle   TEXT PRIMARY KEY,
    valeur TEXT NOT NULL
);

INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('nom_ecole',     'Ecole Primaire');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('adresse_ecole', '');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('telephone_ecole','');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('annee_scolaire', '2025-2026');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('slogan',         '');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('devise',         'Ar');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('logo_path',      '');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('montant_droits_defaut',        '0');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('montant_ecolage_defaut',       '0');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('montant_fram_defaut',          '0');
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES ('montant_participation_defaut', '0');
