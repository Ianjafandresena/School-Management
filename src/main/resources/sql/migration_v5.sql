-- ============================================================
-- Migration v5 : Frais scolaires par niveau
-- Définit les montants à payer pour chaque type et niveau
-- ============================================================

CREATE TABLE IF NOT EXISTS frais_scolaires (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    annee_scolaire  TEXT NOT NULL,
    niveau_id       INTEGER NOT NULL,
    type_paiement   TEXT NOT NULL, -- DROITS, ECOLAGE, FRAM, PARTICIPATION
    montant         REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (niveau_id) REFERENCES niveaux(id),
    UNIQUE (annee_scolaire, niveau_id, type_paiement)
);

-- Index pour accélérer la récupération des tarifs lors de la saisie de paiement
CREATE INDEX IF NOT EXISTS idx_frais_niveau ON frais_scolaires(niveau_id, annee_scolaire);
