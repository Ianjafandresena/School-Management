-- Migration v4: Ajout des champs pour les parents (père et mère)
ALTER TABLE eleves ADD COLUMN nom_pere TEXT;
ALTER TABLE eleves ADD COLUMN tel_pere TEXT;
ALTER TABLE eleves ADD COLUMN nom_mere TEXT;
ALTER TABLE eleves ADD COLUMN tel_mere TEXT;
ALTER TABLE eleves ADD COLUMN nom_tuteur TEXT;
ALTER TABLE eleves ADD COLUMN tel_tuteur TEXT;
ALTER TABLE eleves ADD COLUMN type_tuteur TEXT; -- Pere, Mere, Tuteur, Tutrice
