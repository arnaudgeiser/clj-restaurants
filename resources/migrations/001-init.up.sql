DROP TABLE IF EXISTS RESTAURANTS CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS TYPES_GASTRONOMIQUES CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS VILLES CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS COMMENTAIRES CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS LIKES CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS NOTES CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS CRITERES_EVALUATION CASCADE CONSTRAINTS;

CREATE TABLE RESTAURANTS (numero number(10) auto_increment NOT NULL, nom varchar2(100) NOT NULL, adresse varchar2(100) NOT NULL, description varchar2(2000), site_web varchar2(100), fk_type number(10) NOT NULL, fk_vill number(10) NOT NULL, PRIMARY KEY (numero));
CREATE TABLE TYPES_GASTRONOMIQUES (numero number(10) auto_increment NOT NULL, libelle varchar2(100) NOT NULL UNIQUE, description varchar2(2000) NOT NULL, PRIMARY KEY (numero));
CREATE TABLE VILLES (numero number(10) auto_increment NOT NULL, code_postal varchar2(100) NOT NULL, nom_ville varchar2(100) NOT NULL, PRIMARY KEY (numero));
CREATE TABLE COMMENTAIRES (numero number(10) auto_increment NOT NULL, date_eval date NOT NULL, commentaire varchar2(2000) NOT NULL, nom_utilisateur varchar2(100) NOT NULL, fk_rest number(10), PRIMARY KEY (numero));
CREATE TABLE LIKES (numero number(10) auto_increment NOT NULL, appreciation char(1) NOT NULL, date_eval date NOT NULL, adresse_ip varchar2(100) NOT NULL, fk_rest number(10) NOT NULL, PRIMARY KEY (numero));
CREATE TABLE NOTES (numero number(10) auto_increment NOT NULL, note number(3) NOT NULL, fk_comm number(10) NOT NULL, fk_crit number(10) NOT NULL, PRIMARY KEY (numero));
CREATE TABLE CRITERES_EVALUATION (numero number(10) auto_increment NOT NULL, nom varchar2(100) NOT NULL UNIQUE, description varchar2(512), PRIMARY KEY (numero));

ALTER TABLE RESTAURANTS ADD CONSTRAINT FK_REST_TYPE FOREIGN KEY (fk_type) REFERENCES TYPES_GASTRONOMIQUES (numero);
ALTER TABLE RESTAURANTS ADD CONSTRAINT FK_REST_VILL FOREIGN KEY (fk_vill) REFERENCES VILLES (numero);
ALTER TABLE COMMENTAIRES ADD CONSTRAINT FK_COMM_REST FOREIGN KEY (fk_rest) REFERENCES RESTAURANTS (numero);
ALTER TABLE NOTES ADD CONSTRAINT FK_NOTE_COMM FOREIGN KEY (fk_comm) REFERENCES COMMENTAIRES (numero);
ALTER TABLE NOTES ADD CONSTRAINT FK_NOTE_CRIT FOREIGN KEY (fk_crit) REFERENCES CRITERES_EVALUATION (numero);
ALTER TABLE LIKES ADD CONSTRAINT FK_LIKE_REST FOREIGN KEY (fk_rest) REFERENCES RESTAURANTS (numero);

INSERT INTO TYPES_GASTRONOMIQUES(libelle, description) VALUES ('Cuisine suisse', 'Cuisine classique et plats typiquement suisses');
INSERT INTO TYPES_GASTRONOMIQUES(libelle, description) VALUES ('Restaurant gastronomique', 'Restaurant gastronomique de haut standing');
INSERT INTO TYPES_GASTRONOMIQUES(libelle, description) VALUES ('Pizzeria', 'Pizzas et autres spécialités italiennes');

INSERT INTO CRITERES_EVALUATION(nom, description) VALUES ('Service', 'Qualité du service');
INSERT INTO CRITERES_EVALUATION(nom, description) VALUES ('Cuisine', 'Qualité de la nourriture');
INSERT INTO CRITERES_EVALUATION(nom, description) VALUES ('Cadre', 'L''ambiance et la décoration sont-elles bonnes ?');

INSERT INTO VILLES(code_postal, nom_ville) VALUES ('2000', 'Neuchâtel');

INSERT INTO RESTAURANTS(nom, adresse, description, site_web, fk_type, fk_vill) VALUES ('Fleur-de-Lys', 'Rue du Bassin 10', 'Pizzeria au centre de Neuchâtel', 'http://www.pizzeria-neuchatel.ch', 3, 1);
INSERT INTO RESTAURANTS(nom, adresse, description, site_web, fk_type, fk_vill) VALUES ('La Maison du Prussien', 'Rue des Tunnels 11', 'Restaurant gastronomique renommé de Neuchâtel', 'www.hotel-prussien.ch', 2, 1);

INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Génial !', 'Toto', 1);
INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Très bon', 'Titi', 1);
INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Un régal !', 'Dupont', 2);
INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Rien à dire, le top !', 'Dupasquier', 2);

INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 1, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 1, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 1, 3);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 2, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 2, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 2, 3);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 3, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 3, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 3, 3);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 4, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 4, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 4, 3);

INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.4', 1);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.5', 1);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('F', sysdate, '1.2.3.6', 1);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.7', 2);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.8', 2);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.9', 2);
