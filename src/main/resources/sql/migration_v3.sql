-- ============================================================
-- Migration v3 : Gestion des Matieres et multi-matieres pour enseignants
-- ============================================================

-- Table des matieres
CREATE TABLE IF NOT EXISTS matieres (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    code    TEXT NOT NULL UNIQUE,
    libelle TEXT NOT NULL
);

-- Insertion des matieres par defaut
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('FRANCAIS', 'Français');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('MATHS', 'Mathématiques');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('MALAGASY', 'Malagasy');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('EVY', 'Eveil (EVY)');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('EPS', 'Ed. Physique et Sportive');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('EDHC', 'Ed. aux Droits de l''Homme et Citoyenneté');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('ANGLAIS', 'Anglais');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('HIST-GEO', 'Histoire et Géographie');
INSERT OR IGNORE INTO matieres (code, libelle) VALUES ('SCIENCES', 'Sciences de la Vie et de la Terre');

-- Table de liaison Enseignant <=> Matieres (pour le multi-matieres)
CREATE TABLE IF NOT EXISTS enseignant_matieres (
    enseignant_id INTEGER NOT NULL,
    matiere_id    INTEGER NOT NULL,
    PRIMARY KEY (enseignant_id, matiere_id),
    FOREIGN KEY (enseignant_id) REFERENCES enseignants(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id)    REFERENCES matieres(id) ON DELETE CASCADE
);
