-- ============================================================================
-- ATLANTIC PALACE PRESSING — SYSTÈME CENTRALISÉ MULTI-AGENCES (v3.0)
-- Fichier 01 : Création de la base MySQL 8+, des 18 tables (MLD) et des INDEX
-- Encodage : UTF-8 (utf8mb4) | Moteur : InnoDB
-- ============================================================================

SET NAMES utf8mb4;
SET time_zone = '+01:00'; -- Fuseau horaire Cameroun (Yaoundé, UTC+1)

CREATE DATABASE IF NOT EXISTS atlantic_palace_pressing
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE atlantic_palace_pressing;

-- ============================================================================
-- 1. TABLE AGENCE (6 agences commerciales de Yaoundé + 1 Atelier central)
-- ============================================================================
CREATE TABLE IF NOT EXISTS agence (
    id_agence       INT AUTO_INCREMENT PRIMARY KEY,
    code_agence     VARCHAR(4)   NOT NULL,
    nom_agence      VARCHAR(30)  NOT NULL,
    type_agence     VARCHAR(20)  NOT NULL DEFAULT 'Agence commerciale',
    adresse_agence  VARCHAR(120) NULL,
    tel_agence      VARCHAR(15)  NULL,
    CONSTRAINT uq_agence_code UNIQUE (code_agence),
    CONSTRAINT chk_agence_type CHECK (type_agence IN ('Agence commerciale', 'Atelier central'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 2. TABLE UTILISATEUR (Authentification nominative BCrypt — RG24, CI16, CI17)
-- ============================================================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur      INT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur     VARCHAR(50) NOT NULL,
    login_utilisateur   VARCHAR(30) NOT NULL,
    mot_de_passe_hash   VARCHAR(60) NOT NULL COMMENT 'Empreinte BCrypt uniquement, jamais en clair',
    profil              VARCHAR(15) NOT NULL,
    statut_compte       VARCHAR(10) NOT NULL DEFAULT 'Actif',
    id_agence           INT         NULL COMMENT 'NULL pour la Direction Générale (vue globale RG15)',
    CONSTRAINT uq_utilisateur_login UNIQUE (login_utilisateur),
    CONSTRAINT chk_utilisateur_profil CHECK (profil IN ('DG', 'Réceptionniste', 'Agent', 'Livreur')),
    CONSTRAINT chk_utilisateur_statut CHECK (statut_compte IN ('Actif', 'Désactivé')),
    CONSTRAINT fk_utilisateur_agence FOREIGN KEY (id_agence)
        REFERENCES agence (id_agence) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. TABLE CLIENT (Téléphone normalisé +237... et solde points — RG9, CI2, CI15)
-- ============================================================================
CREATE TABLE IF NOT EXISTS client (
    id_client       INT AUTO_INCREMENT PRIMARY KEY,
    nom_client      VARCHAR(50) NOT NULL,
    tel_client      VARCHAR(15) NOT NULL COMMENT 'Normalisé international (+237XXXXXXXXX)',
    points_fidelite INT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_client_tel UNIQUE (tel_client),
    CONSTRAINT chk_client_points CHECK (points_fidelite >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. TABLE PRESTATION (Catalogue des services — CI10)
-- ============================================================================
CREATE TABLE IF NOT EXISTS prestation (
    id_prestation       INT AUTO_INCREMENT PRIMARY KEY,
    libelle_prestation  VARCHAR(30) NOT NULL,
    duree_estimee       INT         NULL COMMENT 'Durée standard estimée en heures',
    CONSTRAINT uq_prestation_libelle UNIQUE (libelle_prestation),
    CONSTRAINT chk_prestation_duree CHECK (duree_estimee IS NULL OR duree_estimee > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. TABLE TARIF (Grille tarifaire historisée — RG17, RG23, CI14)
-- ============================================================================
CREATE TABLE IF NOT EXISTS tarif (
    id_tarif            INT AUTO_INCREMENT PRIMARY KEY,
    prix_unitaire       DECIMAL(10, 2) NOT NULL,
    date_debut_validite DATE           NOT NULL,
    date_fin_validite   DATE           NULL,
    id_prestation       INT            NOT NULL,
    CONSTRAINT chk_tarif_prix CHECK (prix_unitaire >= 0),
    CONSTRAINT chk_tarif_dates CHECK (date_fin_validite IS NULL OR date_fin_validite > date_debut_validite),
    CONSTRAINT fk_tarif_prestation FOREIGN KEY (id_prestation)
        REFERENCES prestation (id_prestation) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6. TABLE PARAMETRE (Réglages centralisés gérés par le DG — RG18, CI11)
-- ============================================================================
CREATE TABLE IF NOT EXISTS parametre (
    id_parametre    INT AUTO_INCREMENT PRIMARY KEY,
    categorie_param VARCHAR(20)  NOT NULL,
    code_param      VARCHAR(30)  NOT NULL,
    libelle_param   VARCHAR(100) NOT NULL,
    valeur_param    VARCHAR(255) NOT NULL,
    date_maj_param  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    id_utilisateur  INT          NULL COMMENT 'Dernier DG ayant modifié le paramètre',
    CONSTRAINT uq_parametre_code UNIQUE (code_param),
    CONSTRAINT fk_parametre_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7. TABLE ABONNEMENT (Extension v2 préparée dès v1 — BF9, RG19, RG28, CI20)
-- Créée avant COMMANDE pour respecter l'ordre des clés étrangères InnoDB
-- ============================================================================
CREATE TABLE IF NOT EXISTS abonnement (
    id_abonnement       INT AUTO_INCREMENT PRIMARY KEY,
    libelle_abonnement  VARCHAR(40)    NOT NULL,
    cout_mensuel        DECIMAL(10, 2) NOT NULL,
    nb_lavages_inclus   INT            NOT NULL,
    nb_lavages_restants INT            NOT NULL,
    date_debut_abt      DATE           NOT NULL,
    date_fin_abt        DATE           NOT NULL,
    statut_abonnement   VARCHAR(12)    NOT NULL DEFAULT 'Actif',
    id_client           INT            NOT NULL,
    CONSTRAINT chk_abt_cout CHECK (cout_mensuel >= 0),
    CONSTRAINT chk_abt_inclus CHECK (nb_lavages_inclus > 0),
    CONSTRAINT chk_abt_restants CHECK (nb_lavages_restants >= 0),
    CONSTRAINT chk_abt_dates CHECK (date_fin_abt > date_debut_abt),
    CONSTRAINT chk_abt_statut CHECK (statut_abonnement IN ('Actif', 'Suspendu', 'Expiré')),
    CONSTRAINT fk_abonnement_client FOREIGN KEY (id_client)
        REFERENCES client (id_client) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 8. TABLE BON_REDUCTION (Extension v2 préparée dès v1 — BF7, RG29, CI20)
-- Créée avant COMMANDE pour respecter l'ordre des clés étrangères InnoDB
-- ============================================================================
CREATE TABLE IF NOT EXISTS bon_reduction (
    id_bon          INT AUTO_INCREMENT PRIMARY KEY,
    code_bon        VARCHAR(12)    NOT NULL,
    valeur_bon      DECIMAL(10, 2) NOT NULL,
    date_debut_bon  DATE           NOT NULL,
    date_fin_bon    DATE           NOT NULL,
    statut_bon      VARCHAR(10)    NOT NULL DEFAULT 'Actif',
    id_utilisateur  INT            NOT NULL COMMENT 'DG émetteur du bon',
    CONSTRAINT uq_bon_code UNIQUE (code_bon),
    CONSTRAINT chk_bon_valeur CHECK (valeur_bon > 0),
    CONSTRAINT chk_bon_dates CHECK (date_fin_bon >= date_debut_bon),
    CONSTRAINT chk_bon_statut CHECK (statut_bon IN ('Actif', 'Utilisé', 'Expiré')),
    CONSTRAINT fk_bon_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 9. TABLE COMMANDE (Entité centrale — bordereau AG-AA-NNNNNN — RG8, RG13, RG26)
-- ============================================================================
CREATE TABLE IF NOT EXISTS commande (
    num_facture         VARCHAR(14)    PRIMARY KEY COMMENT 'Format AG-AA-NNNNNN (RG26, CI19)',
    date_depot          DATE           NOT NULL,
    statut_cmd          VARCHAR(20)    NOT NULL DEFAULT 'Réceptionné',
    taux_remise         DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    montant_total       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    id_client           INT            NOT NULL,
    id_agence           INT            NOT NULL,
    id_utilisateur      INT            NOT NULL,
    id_abonnement       INT            NULL COMMENT 'v2 : association UTILISER (RG28)',
    date_utilisation    DATE           NULL COMMENT 'v2 : date consommation lavage abonné',
    id_bon              INT            NULL COMMENT 'v2 : association APPLIQUER (RG29)',
    CONSTRAINT uq_commande_bon UNIQUE (id_bon), -- Un bon s'applique à au plus (0,1) commande (CI20)
    CONSTRAINT chk_cmd_statut CHECK (statut_cmd IN ('Réceptionné', 'En lavage', 'En repassage', 'Prêt', 'Retiré', 'Livré')),
    CONSTRAINT chk_cmd_remise CHECK (taux_remise BETWEEN 0 AND 100),
    CONSTRAINT chk_cmd_montant CHECK (montant_total >= 0),
    CONSTRAINT fk_commande_client FOREIGN KEY (id_client)
        REFERENCES client (id_client) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_commande_agence FOREIGN KEY (id_agence)
        REFERENCES agence (id_agence) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_commande_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_commande_abonnement FOREIGN KEY (id_abonnement)
        REFERENCES abonnement (id_abonnement) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_commande_bon FOREIGN KEY (id_bon)
        REFERENCES bon_reduction (id_bon) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 10. TABLE ARTICLE (Vêtements déposés avec état constaté — BF3, BF4, CI4)
-- ============================================================================
CREATE TABLE IF NOT EXISTS article (
    id_article      INT AUTO_INCREMENT PRIMARY KEY,
    etat_reception  VARCHAR(100)   NULL COMMENT 'Preuve contradictoire des défauts constatés au dépôt',
    prix_article    DECIMAL(10, 2) NOT NULL,
    statut_article  VARCHAR(20)    NOT NULL DEFAULT 'Réceptionné',
    num_facture     VARCHAR(14)    NOT NULL,
    id_prestation   INT            NOT NULL,
    id_tarif        INT            NOT NULL,
    CONSTRAINT chk_article_prix CHECK (prix_article >= 0),
    CONSTRAINT chk_article_statut CHECK (statut_article IN ('Réceptionné', 'En lavage', 'En repassage', 'Prêt', 'Retiré', 'Livré')),
    CONSTRAINT fk_article_commande FOREIGN KEY (num_facture)
        REFERENCES commande (num_facture) ON UPDATE CASCADE ON DELETE RESTRICT, -- CI4 : suppression commande interdite
    CONSTRAINT fk_article_prestation FOREIGN KEY (id_prestation)
        REFERENCES prestation (id_prestation) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_article_tarif FOREIGN KEY (id_tarif)
        REFERENCES tarif (id_tarif) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 11. TABLE HISTORIQUE_STATUT (Traçabilité immuable — BF4, RG25, CI18)
-- ============================================================================
CREATE TABLE IF NOT EXISTS historique_statut (
    id_historique       INT AUTO_INCREMENT PRIMARY KEY,
    ancien_statut       VARCHAR(20) NOT NULL,
    nouveau_statut      VARCHAR(20) NOT NULL,
    date_heure_statut   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_article          INT         NOT NULL,
    id_utilisateur      INT         NOT NULL,
    CONSTRAINT chk_hist_ancien CHECK (ancien_statut IN ('Dépôt initial', 'Réceptionné', 'En lavage', 'En repassage', 'Prêt', 'Retiré', 'Livré')),
    CONSTRAINT chk_hist_nouveau CHECK (nouveau_statut IN ('Réceptionné', 'En lavage', 'En repassage', 'Prêt', 'Retiré', 'Livré')),
    CONSTRAINT fk_historique_article FOREIGN KEY (id_article)
        REFERENCES article (id_article) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_historique_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 12. TABLE PAIEMENT (Acomptes et solde — BF6, RG16, CI9)
-- ============================================================================
CREATE TABLE IF NOT EXISTS paiement (
    id_paiement     INT AUTO_INCREMENT PRIMARY KEY,
    date_paiement   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_paye    DECIMAL(10, 2) NOT NULL,
    mode_paiement   VARCHAR(15)    NOT NULL,
    num_facture     VARCHAR(14)    NOT NULL,
    CONSTRAINT chk_paiement_montant CHECK (montant_paye > 0),
    CONSTRAINT chk_paiement_mode CHECK (mode_paiement IN ('Espèces', 'Mobile Money', 'Carte')),
    CONSTRAINT fk_paiement_commande FOREIGN KEY (num_facture)
        REFERENCES commande (num_facture) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 13. TABLE NOTIFICATION (1 notification SMS par passage à « Prêt » — RG30, CI21)
-- ============================================================================
CREATE TABLE IF NOT EXISTS notification (
    id_notification INT AUTO_INCREMENT PRIMARY KEY,
    date_envoi_sms  DATETIME    NULL,
    statut_sms      VARCHAR(10) NOT NULL DEFAULT 'En attente',
    nb_renvois      INT         NOT NULL DEFAULT 0,
    num_facture     VARCHAR(14) NOT NULL COMMENT 'NON unique : plusieurs passages à Prêt possibles (RG30)',
    CONSTRAINT chk_notif_statut CHECK (statut_sms IN ('En attente', 'Envoyé', 'Échec')),
    CONSTRAINT chk_notif_renvois CHECK (nb_renvois >= 0),
    CONSTRAINT fk_notification_commande FOREIGN KEY (num_facture)
        REFERENCES commande (num_facture) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 14. TABLE JOURNAL (Audit nominatif non modifiable — BNF2, RG14)
-- ============================================================================
CREATE TABLE IF NOT EXISTS journal (
    id_journal      INT AUTO_INCREMENT PRIMARY KEY,
    date_heure      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action_journal  VARCHAR(100) NOT NULL,
    cible_type      VARCHAR(30)  NULL,
    cible_id        VARCHAR(14)  NULL,
    id_utilisateur  INT          NOT NULL,
    CONSTRAINT chk_journal_cible CHECK (cible_type IS NULL OR cible_id IS NOT NULL),
    CONSTRAINT fk_journal_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 15. TABLE RECLAMATION (Suivi SAV sans redondance id_client — BF11, RG20)
-- ============================================================================
CREATE TABLE IF NOT EXISTS reclamation (
    id_reclamation      INT AUTO_INCREMENT PRIMARY KEY,
    motif               VARCHAR(50)  NOT NULL,
    description_recl    VARCHAR(255) NULL,
    statut_reclamation  VARCHAR(15)  NOT NULL DEFAULT 'Ouverte',
    date_reclamation    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_resolution     DATETIME     NULL,
    num_facture         VARCHAR(14)  NOT NULL,
    id_utilisateur      INT          NULL COMMENT 'Utilisateur instruisant la réclamation',
    CONSTRAINT chk_recl_statut CHECK (statut_reclamation IN ('Ouverte', 'En cours', 'Résolue')),
    CONSTRAINT fk_reclamation_commande FOREIGN KEY (num_facture)
        REFERENCES commande (num_facture) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_reclamation_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 16. TABLE PRODUIT (Catalogue consommables v2 — BF12, RG21)
-- ============================================================================
CREATE TABLE IF NOT EXISTS produit (
    id_produit      INT AUTO_INCREMENT PRIMARY KEY,
    libelle_produit VARCHAR(40) NOT NULL,
    categorie       VARCHAR(25) NULL,
    unite_mesure    VARCHAR(12) NULL,
    seuil_alerte    INT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_produit_libelle UNIQUE (libelle_produit),
    CONSTRAINT chk_produit_seuil CHECK (seuil_alerte >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 17. TABLE MOUVEMENT_STOCK (Entrées / Sorties par site v2 — BF12, RG21, CI13)
-- ============================================================================
CREATE TABLE IF NOT EXISTS mouvement_stock (
    id_mouvement    INT AUTO_INCREMENT PRIMARY KEY,
    type_mouvement  VARCHAR(8)   NOT NULL,
    quantite        INT          NOT NULL,
    date_mouvement  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motif           VARCHAR(100) NULL,
    id_produit      INT          NOT NULL,
    id_agence       INT          NOT NULL,
    id_utilisateur  INT          NOT NULL,
    CONSTRAINT chk_mvt_type CHECK (type_mouvement IN ('Entrée', 'Sortie')),
    CONSTRAINT chk_mvt_quantite CHECK (quantite > 0),
    CONSTRAINT fk_mouvement_produit FOREIGN KEY (id_produit)
        REFERENCES produit (id_produit) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_mouvement_agence FOREIGN KEY (id_agence)
        REFERENCES agence (id_agence) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_mouvement_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 18. TABLE LIVRAISON (Livraison à domicile v2 — BF10, RG22, CI12)
-- ============================================================================
CREATE TABLE IF NOT EXISTS livraison (
    id_livraison        INT AUTO_INCREMENT PRIMARY KEY,
    adresse_livraison   VARCHAR(120)  NOT NULL,
    statut_livraison    VARCHAR(12)   NOT NULL DEFAULT 'Planifiée',
    date_souhaitee      DATE          NULL,
    frais_livraison     DECIMAL(7, 2) NOT NULL DEFAULT 0.00,
    num_facture         VARCHAR(14)   NOT NULL,
    id_utilisateur      INT           NULL COMMENT 'Livreur assigné',
    CONSTRAINT uq_livraison_commande UNIQUE (num_facture), -- Au plus (0,1) livraison par commande (RG22)
    CONSTRAINT chk_livraison_statut CHECK (statut_livraison IN ('Planifiée', 'En cours', 'Livrée', 'Échec')),
    CONSTRAINT chk_livraison_frais CHECK (frais_livraison >= 0),
    CONSTRAINT fk_livraison_commande FOREIGN KEY (num_facture)
        REFERENCES commande (num_facture) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_livraison_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur (id_utilisateur) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- INDEX D'OPTIMISATION DES PERFORMANCES (BNF1 < 1 min, BF4, BF8, RG15)
-- ============================================================================

-- Recherche rapide d'un client par nom au comptoir (BNF1, hypothèse 7.2 numéros partagés)
CREATE INDEX idx_client_nom
    ON client (nom_client);

-- Cloisonnement rapide des utilisateurs par agence, profil et statut actif (RG15, CI17)
CREATE INDEX idx_utilisateur_agence_profil
    ON utilisateur (id_agence, profil, statut_compte);

-- Filtrage par agence + période + statut (tableaux de bord réception & reporting DG BF8)
CREATE INDEX idx_commande_agence_date_statut
    ON commande (id_agence, date_depot, statut_cmd);

-- Historique des commandes d'un client donné (BF2)
CREATE INDEX idx_commande_client_date
    ON commande (id_client, date_depot DESC);

-- File de travail de l'atelier central par statut de commande (BF4)
CREATE INDEX idx_commande_statut
    ON commande (statut_cmd);

-- Contrôle instantané de complétude : tous les articles d'une commande sont-ils « Prêt » ? (RG11)
CREATE INDEX idx_article_commande_statut
    ON article (num_facture, statut_article);

-- Reconstitution chronologique du cycle de vie d'un article et d'une commande (BF4, RG25)
CREATE INDEX idx_historique_article_date
    ON historique_statut (id_article, date_heure_statut ASC);

-- Sommation des acomptes et calcul du CA encaissé par période (RG16, BF8)
CREATE INDEX idx_paiement_commande_date
    ON paiement (num_facture, date_paiement);

CREATE INDEX idx_paiement_date
    ON paiement (date_paiement);

-- File d'attente d'envoi et de renvoi automatique des SMS (BF5, BNF9, RG30)
CREATE INDEX idx_notification_statut_renvois
    ON notification (statut_sms, nb_renvois);

CREATE INDEX idx_notification_commande
    ON notification (num_facture, date_envoi_sms DESC);

-- Recherche de la version de tarif en vigueur à la date du dépôt (RG17, RG23, CI14)
CREATE INDEX idx_tarif_prestation_validite
    ON tarif (id_prestation, date_debut_validite, date_fin_validite);

-- Audit de sécurité par période/utilisateur et par objet cible (RG14, BNF2)
CREATE INDEX idx_journal_date_utilisateur
    ON journal (date_heure DESC, id_utilisateur);

CREATE INDEX idx_journal_cible
    ON journal (cible_type, cible_id);

-- Suivi qualité des réclamations ouvertes / en cours (BF11)
CREATE INDEX idx_reclamation_statut_date
    ON reclamation (statut_reclamation, date_reclamation DESC);

-- Extensions v2 : abonnements actifs et calcul des stocks par site (BF9, BF12)
CREATE INDEX idx_abonnement_client_statut
    ON abonnement (id_client, statut_abonnement, date_fin_abt);

CREATE INDEX idx_mouvement_agence_produit
    ON mouvement_stock (id_agence, id_produit, type_mouvement);
