-- ============================================================================
-- ATLANTIC PALACE PRESSING — SYSTÈME CENTRALISÉ MULTI-AGENCES (v3.0)
-- Fichier 02 : Fonctions stockées, Triggers d'intégrité et Procédures (DELIMITER)
-- Implémentation stricte des règles Merise RG1..RG30 et contraintes CI1..CI21
-- ============================================================================

SET NAMES utf8mb4;
USE atlantic_palace_pressing;

-- Pour autoriser la création de fonctions même si le binary logging MySQL est actif
SET GLOBAL log_bin_trust_function_creators = 1;

-- ============================================================================
-- NETTOYAGE IDEMPOTENT AVANT CRÉATION
-- ============================================================================
DROP FUNCTION IF EXISTS fn_normaliser_telephone;
DROP FUNCTION IF EXISTS fn_generer_num_facture;
DROP FUNCTION IF EXISTS fn_obtenir_id_tarif_actif;
DROP FUNCTION IF EXISTS fn_calculer_montant_total;
DROP FUNCTION IF EXISTS fn_somme_paiements;
DROP FUNCTION IF EXISTS fn_reste_a_payer;
DROP FUNCTION IF EXISTS fn_commande_tous_articles_prets;
DROP FUNCTION IF EXISTS fn_stock_disponible;

DROP TRIGGER IF EXISTS trg_historique_statut_no_update;
DROP TRIGGER IF EXISTS trg_historique_statut_no_delete;
DROP TRIGGER IF EXISTS trg_journal_no_update;
DROP TRIGGER IF EXISTS trg_journal_no_delete;
DROP TRIGGER IF EXISTS trg_client_before_insert_tel;
DROP TRIGGER IF EXISTS trg_client_before_update_tel;
DROP TRIGGER IF EXISTS trg_commande_before_delete;
DROP TRIGGER IF EXISTS trg_tarif_before_insert_validite;

DROP PROCEDURE IF EXISTS sp_journaliser_action;
DROP PROCEDURE IF EXISTS sp_creer_commande;
DROP PROCEDURE IF EXISTS sp_ajouter_article_commande;
DROP PROCEDURE IF EXISTS sp_changer_statut_article;
DROP PROCEDURE IF EXISTS sp_enregistrer_paiement;
DROP PROCEDURE IF EXISTS sp_valider_retrait_commande;
DROP PROCEDURE IF EXISTS sp_rapport_pilotage_dg;

-- ============================================================================
-- SECTION 1 : FONCTIONS STOCKÉES (FUNCTIONS) AVEC DELIMITER $$
-- ============================================================================

DELIMITER $$

-- ----------------------------------------------------------------------------
-- 1.1 fn_normaliser_telephone : Normalise un numéro camerounais à 9 chiffres
--     en format international +237XXXXXXXXX (RG9, CI2)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_normaliser_telephone(p_tel VARCHAR(25))
RETURNS VARCHAR(15)
DETERMINISTIC
BEGIN
    DECLARE v_clean VARCHAR(25);

    IF p_tel IS NULL THEN
        RETURN NULL;
    END IF;

    -- Supprimer espaces, tirets, points et parenthèses
    SET v_clean = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(p_tel), ' ', ''), '-', ''), '.', ''), '(', ''), ')', '');

    -- Si le préfixe 00237 est présent, le convertir en +237
    IF LEFT(v_clean, 5) = '00237' THEN
        SET v_clean = CONCAT('+237', SUBSTRING(v_clean, 6));
    ELSEIF LEFT(v_clean, 3) = '237' AND CHAR_LENGTH(v_clean) = 12 THEN
        SET v_clean = CONCAT('+', v_clean);
    ELSEIF CHAR_LENGTH(v_clean) = 9 THEN
        SET v_clean = CONCAT('+237', v_clean);
    END IF;

    RETURN LEFT(v_clean, 15);
END$$

-- ----------------------------------------------------------------------------
-- 1.2 fn_generer_num_facture : Génère un numéro unique AG-AA-NNNNNN
--     Ex : YA1-26-000001 (RG8, RG26, CI19)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_generer_num_facture(p_id_agence INT, p_date_depot DATE)
RETURNS VARCHAR(14)
READS SQL DATA
BEGIN
    DECLARE v_code_agence VARCHAR(4);
    DECLARE v_annee       CHAR(2);
    DECLARE v_prefixe     VARCHAR(8);
    DECLARE v_dernier_seq INT DEFAULT 0;

    SELECT code_agence INTO v_code_agence
    FROM agence
    WHERE id_agence = p_id_agence;

    IF v_code_agence IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI3 / RG26 : Agence introuvable pour la génération du bordereau.';
    END IF;

    SET v_annee   = DATE_FORMAT(COALESCE(p_date_depot, CURDATE()), '%y');
    SET v_prefixe = CONCAT(UPPER(v_code_agence), '-', v_annee, '-');

    SELECT COALESCE(MAX(CAST(RIGHT(num_facture, 6) AS UNSIGNED)), 0)
    INTO v_dernier_seq
    FROM commande
    WHERE num_facture LIKE CONCAT(v_prefixe, '%');

    RETURN CONCAT(v_prefixe, LPAD(v_dernier_seq + 1, 6, '0'));
END$$

-- ----------------------------------------------------------------------------
-- 1.3 fn_obtenir_id_tarif_actif : Retourne la version de tarif applicable à
--     une prestation pour une date de dépôt donnée (RG17, RG23, CI14)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_obtenir_id_tarif_actif(p_id_prestation INT, p_date_depot DATE)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_id_tarif INT DEFAULT NULL;

    SELECT id_tarif INTO v_id_tarif
    FROM tarif
    WHERE id_prestation = p_id_prestation
      AND date_debut_validite <= COALESCE(p_date_depot, CURDATE())
      AND (date_fin_validite IS NULL OR date_fin_validite >= COALESCE(p_date_depot, CURDATE()))
    ORDER BY date_debut_validite DESC
    LIMIT 1;

    RETURN v_id_tarif;
END$$

-- ----------------------------------------------------------------------------
-- 1.4 fn_calculer_montant_total : Formule officielle RG13 + RG22 + RG29 + CI7
--     total = (somme(prix_article) * (1 - taux_remise / 100)) - valeur_bon + frais_livraison
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_calculer_montant_total(p_num_facture VARCHAR(14))
RETURNS DECIMAL(10, 2)
READS SQL DATA
BEGIN
    DECLARE v_somme_articles  DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_taux_remise     DECIMAL(5, 2)  DEFAULT 0.00;
    DECLARE v_frais_livraison DECIMAL(7, 2)  DEFAULT 0.00;
    DECLARE v_valeur_bon      DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_id_bon          INT            DEFAULT NULL;
    DECLARE v_total           DECIMAL(10, 2) DEFAULT 0.00;

    SELECT COALESCE(SUM(prix_article), 0.00)
    INTO v_somme_articles
    FROM article
    WHERE num_facture = p_num_facture;

    SELECT COALESCE(taux_remise, 0.00), id_bon
    INTO v_taux_remise, v_id_bon
    FROM commande
    WHERE num_facture = p_num_facture;

    SELECT COALESCE(frais_livraison, 0.00)
    INTO v_frais_livraison
    FROM livraison
    WHERE num_facture = p_num_facture
    LIMIT 1;

    IF v_id_bon IS NOT NULL THEN
        SELECT COALESCE(valeur_bon, 0.00)
        INTO v_valeur_bon
        FROM bon_reduction
        WHERE id_bon = v_id_bon;
    END IF;

    SET v_total = ROUND((v_somme_articles * (1.00 - (v_taux_remise / 100.00))) - v_valeur_bon + v_frais_livraison, 2);

    -- CI7 : le montant total n'est jamais négatif
    IF v_total < 0.00 THEN
        SET v_total = 0.00;
    END IF;

    RETURN v_total;
END$$

-- ----------------------------------------------------------------------------
-- 1.5 fn_somme_paiements : Somme des acomptes et règlements d'une commande (RG16)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_somme_paiements(p_num_facture VARCHAR(14))
RETURNS DECIMAL(10, 2)
READS SQL DATA
BEGIN
    DECLARE v_somme DECIMAL(10, 2) DEFAULT 0.00;

    SELECT COALESCE(SUM(montant_paye), 0.00)
    INTO v_somme
    FROM paiement
    WHERE num_facture = p_num_facture;

    RETURN v_somme;
END$$

-- ----------------------------------------------------------------------------
-- 1.6 fn_reste_a_payer : Solde restant à régler avant retrait (BF6, RG16)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_reste_a_payer(p_num_facture VARCHAR(14))
RETURNS DECIMAL(10, 2)
READS SQL DATA
BEGIN
    DECLARE v_montant_total DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_paye          DECIMAL(10, 2) DEFAULT 0.00;

    SELECT COALESCE(montant_total, 0.00)
    INTO v_montant_total
    FROM commande
    WHERE num_facture = p_num_facture;

    SET v_paye = fn_somme_paiements(p_num_facture);

    IF (v_montant_total - v_paye) < 0.00 THEN
        RETURN 0.00;
    END IF;

    RETURN ROUND(v_montant_total - v_paye, 2);
END$$

-- ----------------------------------------------------------------------------
-- 1.7 fn_commande_tous_articles_prets : Retourne 1 si 100 % des articles de la
--     commande sont au statut « Prêt », 0 sinon (RG11)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_commande_tous_articles_prets(p_num_facture VARCHAR(14))
RETURNS TINYINT(1)
READS SQL DATA
BEGIN
    DECLARE v_nb_total INT DEFAULT 0;
    DECLARE v_nb_prets INT DEFAULT 0;

    SELECT COUNT(*),
           SUM(CASE WHEN statut_article = 'Prêt' THEN 1 ELSE 0 END)
    INTO v_nb_total, v_nb_prets
    FROM article
    WHERE num_facture = p_num_facture;

    IF v_nb_total > 0 AND v_nb_total = v_nb_prets THEN
        RETURN 1;
    END IF;

    RETURN 0;
END$$

-- ----------------------------------------------------------------------------
-- 1.8 fn_stock_disponible : État du stock par sommation des mouvements (RG21)
-- ----------------------------------------------------------------------------
CREATE FUNCTION fn_stock_disponible(p_id_produit INT, p_id_agence INT)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_stock INT DEFAULT 0;

    SELECT COALESCE(SUM(
        CASE
            WHEN type_mouvement = 'Entrée' THEN quantite
            WHEN type_mouvement = 'Sortie' THEN -quantite
            ELSE 0
        END
    ), 0)
    INTO v_stock
    FROM mouvement_stock
    WHERE id_produit = p_id_produit
      AND id_agence  = p_id_agence;

    RETURN v_stock;
END$$


-- ============================================================================
-- SECTION 2 : TRIGGERS D'INTÉGRITÉ ET D'IMMUABILITÉ AVEC DELIMITER $$
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 2.1 Immuabilité stricte de HISTORIQUE_STATUT (RG25, CI18)
-- ----------------------------------------------------------------------------
CREATE TRIGGER trg_historique_statut_no_update
BEFORE UPDATE ON historique_statut
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'RG25 / CI18 : Interdiction absolue de modifier une ligne de historique_statut.';
END$$

CREATE TRIGGER trg_historique_statut_no_delete
BEFORE DELETE ON historique_statut
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'RG25 / CI18 : Interdiction absolue de supprimer une ligne de historique_statut.';
END$$

-- ----------------------------------------------------------------------------
-- 2.2 Immuabilité stricte du JOURNAL d'audit (BNF2, RG14)
-- ----------------------------------------------------------------------------
CREATE TRIGGER trg_journal_no_update
BEFORE UPDATE ON journal
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'BNF2 / RG14 : Le journal d''audit est non modifiable.';
END$$

CREATE TRIGGER trg_journal_no_delete
BEFORE DELETE ON journal
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'BNF2 / RG14 :Aucune ligne du journal d''audit ne peut être supprimée.';
END$$

-- ----------------------------------------------------------------------------
-- 2.3 Normalisation et contrôle automatique du téléphone client (RG9, CI2)
-- ----------------------------------------------------------------------------
CREATE TRIGGER trg_client_before_insert_tel
BEFORE INSERT ON client
FOR EACH ROW
BEGIN
    SET NEW.tel_client = fn_normaliser_telephone(NEW.tel_client);
    IF NEW.tel_client NOT REGEXP '^\\+237[0-9]{9}$' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG9 / CI2 : Le téléphone client doit comporter 9 chiffres camerounais (+237XXXXXXXXX).';
    END IF;
END$$

CREATE TRIGGER trg_client_before_update_tel
BEFORE UPDATE ON client
FOR EACH ROW
BEGIN
    SET NEW.tel_client = fn_normaliser_telephone(NEW.tel_client);
    IF NEW.tel_client NOT REGEXP '^\\+237[0-9]{9}$' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG9 / CI2 : Le téléphone client doit comporter 9 chiffres camerounais (+237XXXXXXXXX).';
    END IF;
END$$

-- ----------------------------------------------------------------------------
-- 2.4 Interdiction explicite de supprimer une commande ayant des articles (CI4)
-- ----------------------------------------------------------------------------
CREATE TRIGGER trg_commande_before_delete
BEFORE DELETE ON commande
FOR EACH ROW
BEGIN
    DECLARE v_nb_articles INT DEFAULT 0;

    SELECT COUNT(*) INTO v_nb_articles
    FROM article
    WHERE num_facture = OLD.num_facture;

    IF v_nb_articles > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI4 / RG5 : Impossible de supprimer une commande contenant déjà des articles.';
    END IF;
END$$

-- ----------------------------------------------------------------------------
-- 2.5 Non-chevauchement des versions de tarif par prestation (RG23, CI14)
-- ----------------------------------------------------------------------------
CREATE TRIGGER trg_tarif_before_insert_validite
BEFORE INSERT ON tarif
FOR EACH ROW
BEGIN
    DECLARE v_conflits INT DEFAULT 0;

    SELECT COUNT(*) INTO v_conflits
    FROM tarif
    WHERE id_prestation = NEW.id_prestation
      AND date_debut_validite <= COALESCE(NEW.date_fin_validite, '9999-12-31')
      AND COALESCE(date_fin_validite, '9999-12-31') >= NEW.date_debut_validite;

    IF v_conflits > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI14 / RG23 : Une autre version de tarif est déjà en vigueur sur cette période pour cette prestation.';
    END IF;
END$$


-- ============================================================================
-- SECTION 3 : PROCÉDURES STOCKÉES MÉTIER (STORED PROCEDURES) AVEC DELIMITER $$
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 3.1 sp_journaliser_action : Journalisation nominative standardisée (RG14)
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_journaliser_action(
    IN p_id_utilisateur INT,
    IN p_action         VARCHAR(100),
    IN p_cible_type     VARCHAR(30),
    IN p_cible_id       VARCHAR(14)
)
MODIFIES SQL DATA
BEGIN
    INSERT INTO journal (date_heure, action_journal, cible_type, cible_id, id_utilisateur)
    VALUES (NOW(), p_action, p_cible_type, p_cible_id, p_id_utilisateur);
END$$

-- ----------------------------------------------------------------------------
-- 3.2 sp_creer_commande : Opération 1 du MCT — Création d'un bordereau de dépôt
--     Gère aussi la numérotation hors ligne fournie ou auto-générée (RG26),
--     le plafond de remise (RG12, RG18), l'abonnement (RG28) et les bons (RG29)
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_creer_commande(
    IN  p_id_client         INT,
    IN  p_id_agence         INT,
    IN  p_id_utilisateur    INT,
    IN  p_taux_remise       DECIMAL(5, 2),
    IN  p_num_facture_sync  VARCHAR(14), -- Optionnel : fourni si créé hors ligne (§7.5)
    IN  p_id_abonnement     INT,         -- Optionnel (v2)
    IN  p_id_bon            INT,         -- Optionnel (v2)
    OUT o_num_facture       VARCHAR(14)
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_statut_compte  VARCHAR(10);
    DECLARE v_profil         VARCHAR(15);
    DECLARE v_plafond_remise DECIMAL(5, 2) DEFAULT 10.00;
    DECLARE v_plafond_txt    VARCHAR(255);
    DECLARE v_restants       INT;
    DECLARE v_statut_bon     VARCHAR(10);

    -- CI17 / RG24 : Vérifier que l'utilisateur existe et est Actif
    SELECT statut_compte, profil
    INTO v_statut_compte, v_profil
    FROM utilisateur
    WHERE id_utilisateur = p_id_utilisateur;

    IF v_statut_compte IS NULL OR v_statut_compte <> 'Actif' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI17 / RG24 : Compte utilisateur inexistant ou désactivé.';
    END IF;

    -- RG12 / RG18 : Vérifier le plafond de remise paramétré par le DG (sauf si le DG opère lui-même)
    SELECT valeur_param INTO v_plafond_txt
    FROM parametre
    WHERE code_param = 'plafond_remise'
    LIMIT 1;

    IF v_plafond_txt IS NOT NULL THEN
        SET v_plafond_remise = CAST(v_plafond_txt AS DECIMAL(5, 2));
    END IF;

    IF COALESCE(p_taux_remise, 0.00) > v_plafond_remise AND v_profil <> 'DG' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG12 : Le taux de remise dépasse le plafond autorisé par la Direction Générale.';
    END IF;

    -- RG26 : Numéro de bordereau (hors ligne si fourni et conforme, sinon généré)
    IF p_num_facture_sync IS NOT NULL AND CHAR_LENGTH(TRIM(p_num_facture_sync)) > 0 THEN
        SET o_num_facture = UPPER(TRIM(p_num_facture_sync));
    ELSE
        SET o_num_facture = fn_generer_num_facture(p_id_agence, CURDATE());
    END IF;

    -- RG28 / CI20 (v2) : Contrôle et décompte d'un lavage d'abonnement actif
    IF p_id_abonnement IS NOT NULL THEN
        SELECT nb_lavages_restants INTO v_restants
        FROM abonnement
        WHERE id_abonnement = p_id_abonnement
          AND id_client = p_id_client
          AND statut_abonnement = 'Actif'
          AND CURDATE() BETWEEN date_debut_abt AND date_fin_abt
        FOR UPDATE;

        IF v_restants IS NULL OR v_restants <= 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RG28 / CI20 : Abonnement invalide, expiré ou sans lavages restants.';
        END IF;

        UPDATE abonnement
        SET nb_lavages_restants = nb_lavages_restants - 1
        WHERE id_abonnement = p_id_abonnement;
    END IF;

    -- RG29 / CI20 (v2) : Contrôle et marquage d'un bon de réduction
    IF p_id_bon IS NOT NULL THEN
        SELECT statut_bon INTO v_statut_bon
        FROM bon_reduction
        WHERE id_bon = p_id_bon
          AND CURDATE() BETWEEN date_debut_bon AND date_fin_bon
        FOR UPDATE;

        IF v_statut_bon IS NULL OR v_statut_bon <> 'Actif' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RG29 / CI20 : Bon de réduction introuvable, expiré ou déjà utilisé.';
        END IF;

        UPDATE bon_reduction
        SET statut_bon = 'Utilisé'
        WHERE id_bon = p_id_bon;
    END IF;

    INSERT INTO commande (
        num_facture, date_depot, statut_cmd, taux_remise, montant_total,
        id_client, id_agence, id_utilisateur, id_abonnement, date_utilisation, id_bon
    ) VALUES (
        o_num_facture,
        CURDATE(),
        'Réceptionné',
        COALESCE(p_taux_remise, 0.00),
        0.00,
        p_id_client,
        p_id_agence,
        p_id_utilisateur,
        p_id_abonnement,
        CASE WHEN p_id_abonnement IS NOT NULL THEN CURDATE() ELSE NULL END,
        p_id_bon
    );

    CALL sp_journaliser_action(
        p_id_utilisateur,
        CONCAT('Création du bordereau ', o_num_facture),
        'COMMANDE',
        o_num_facture
    );
END$$

-- ----------------------------------------------------------------------------
-- 3.3 sp_ajouter_article_commande : Ajoute un vêtement au tarif actif en vigueur,
--     crée sa 1re ligne dans historique_statut (RG25) et recalcule le total (RG13)
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_ajouter_article_commande(
    IN  p_num_facture     VARCHAR(14),
    IN  p_id_prestation   INT,
    IN  p_etat_reception  VARCHAR(100),
    IN  p_id_utilisateur  INT,
    OUT o_id_article      INT
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_date_depot    DATE;
    DECLARE v_id_tarif      INT;
    DECLARE v_prix_unitaire DECIMAL(10, 2);

    SELECT date_depot INTO v_date_depot
    FROM commande
    WHERE num_facture = p_num_facture;

    IF v_date_depot IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI4 : Commande introuvable pour l''ajout d''article.';
    END IF;

    -- RG17 : Récupérer le tarif en vigueur au jour du dépôt
    SET v_id_tarif = fn_obtenir_id_tarif_actif(p_id_prestation, v_date_depot);

    IF v_id_tarif IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI10 / RG17 : Aucun tarif actif trouvé pour cette prestation à la date du dépôt.';
    END IF;

    SELECT prix_unitaire INTO v_prix_unitaire
    FROM tarif
    WHERE id_tarif = v_id_tarif;

    INSERT INTO article (
        etat_reception, prix_article, statut_article, num_facture, id_prestation, id_tarif
    ) VALUES (
        p_etat_reception, v_prix_unitaire, 'Réceptionné', p_num_facture, p_id_prestation, v_id_tarif
    );

    SET o_id_article = LAST_INSERT_ID();

    -- RG25 : Première ligne immuable dans l'historique des statuts (MCT Op. 1)
    INSERT INTO historique_statut (
        ancien_statut, nouveau_statut, date_heure_statut, id_article, id_utilisateur
    ) VALUES (
        'Dépôt initial', 'Réceptionné', NOW(), o_id_article, p_id_utilisateur
    );

    -- RG13 : Recalcul immédiat du montant total de la commande
    UPDATE commande
    SET montant_total = fn_calculer_montant_total(p_num_facture)
    WHERE num_facture = p_num_facture;

    CALL sp_journaliser_action(
        p_id_utilisateur,
        CONCAT('Ajout article #', o_id_article, ' sur bordereau ', p_num_facture),
        'ARTICLE',
        CAST(o_id_article AS CHAR)
    );
END$$

-- ----------------------------------------------------------------------------
-- 3.4 sp_changer_statut_article : Opérations 2 & 3 du MCT — Workflow atelier
--     Contrôle l'ordre RG10, écrit HISTORIQUE_STATUT (RG25), et déclenche
--     automatiquement le passage à « Prêt » + création NOTIFICATION SMS (RG7, RG11, RG30)
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_changer_statut_article(
    IN p_id_article      INT,
    IN p_nouveau_statut  VARCHAR(20),
    IN p_id_utilisateur  INT
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_ancien_statut  VARCHAR(20);
    DECLARE v_num_facture    VARCHAR(14);
    DECLARE v_statut_cmd     VARCHAR(20);
    DECLARE v_statut_compte  VARCHAR(10);
    DECLARE v_transition_ok  TINYINT(1) DEFAULT 0;
    DECLARE v_id_notif       INT;

    SELECT statut_compte INTO v_statut_compte
    FROM utilisateur
    WHERE id_utilisateur = p_id_utilisateur;

    IF v_statut_compte IS NULL OR v_statut_compte <> 'Actif' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI17 / RG24 : Utilisateur non actif.';
    END IF;

    SELECT statut_article, num_facture
    INTO v_ancien_statut, v_num_facture
    FROM article
    WHERE id_article = p_id_article
    FOR UPDATE;

    IF v_ancien_statut IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Article introuvable.';
    END IF;

    SELECT statut_cmd INTO v_statut_cmd
    FROM commande
    WHERE num_facture = v_num_facture
    FOR UPDATE;

    -- RG10 / CI18 + RG30 (retour atelier autorisé de Prêt -> En lavage / En repassage)
    IF (v_ancien_statut = 'Réceptionné'  AND p_nouveau_statut = 'En lavage')
       OR (v_ancien_statut = 'En lavage'    AND p_nouveau_statut = 'En repassage')
       OR (v_ancien_statut = 'En repassage' AND p_nouveau_statut = 'Prêt')
       OR (v_ancien_statut = 'Prêt'         AND p_nouveau_statut IN ('En lavage', 'En repassage')) THEN
        SET v_transition_ok = 1;
    END IF;

    IF v_transition_ok = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG10 / CI18 : Transition de statut invalide (ordre imposé : Réceptionné -> En lavage -> En repassage -> Prêt).';
    END IF;

    -- Mise à jour de l'article
    UPDATE article
    SET statut_article = p_nouveau_statut
    WHERE id_article = p_id_article;

    -- RG25 : Écriture immuable dans HISTORIQUE_STATUT
    INSERT INTO historique_statut (
        ancien_statut, nouveau_statut, date_heure_statut, id_article, id_utilisateur
    ) VALUES (
        v_ancien_statut, p_nouveau_statut, NOW(), p_id_article, p_id_utilisateur
    );

    -- RG11 : Vérifier si TOUS les articles de la commande sont désormais « Prêt »
    IF fn_commande_tous_articles_prets(v_num_facture) = 1 THEN
        IF v_statut_cmd <> 'Prêt' THEN
            UPDATE commande
            SET statut_cmd = 'Prêt'
            WHERE num_facture = v_num_facture;

            -- RG7, RG30, CI8, CI21 : Créer UNE nouvelle notification SMS par passage à « Prêt »
            INSERT INTO notification (date_envoi_sms, statut_sms, nb_renvois, num_facture)
            VALUES (NULL, 'En attente', 0, v_num_facture);

            SET v_id_notif = LAST_INSERT_ID();

            CALL sp_journaliser_action(
                p_id_utilisateur,
                CONCAT('Commande ', v_num_facture, ' passée à Prêt — Notification SMS #', v_id_notif, ' créée'),
                'NOTIFICATION',
                CAST(v_id_notif AS CHAR)
            );
        END IF;
    ELSE
        -- Sinon le statut global reflète l'avancement atelier en cours
        UPDATE commande
        SET statut_cmd = p_nouveau_statut
        WHERE num_facture = v_num_facture
          AND statut_cmd NOT IN ('Retiré', 'Livré');
    END IF;

    CALL sp_journaliser_action(
        p_id_utilisateur,
        CONCAT('Statut article #', p_id_article, ' : ', v_ancien_statut, ' -> ', p_nouveau_statut),
        'ARTICLE',
        CAST(p_id_article AS CHAR)
    );
END$$

-- ----------------------------------------------------------------------------
-- 3.5 sp_enregistrer_paiement : Encaissement acompte ou solde (BF6, RG16, CI9)
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_enregistrer_paiement(
    IN  p_num_facture    VARCHAR(14),
    IN  p_montant_paye   DECIMAL(10, 2),
    IN  p_mode_paiement  VARCHAR(15),
    IN  p_id_utilisateur INT,
    OUT o_id_paiement    INT,
    OUT o_reste_a_payer  DECIMAL(10, 2)
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_reste DECIMAL(10, 2);

    IF p_montant_paye <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CI9 : Le montant encaissé doit être strictement positif.';
    END IF;

    SET v_reste = fn_reste_a_payer(p_num_facture);

    IF p_montant_paye > v_reste THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG16 : Le montant encaissé dépasse le reste à payer de la commande.';
    END IF;

    INSERT INTO paiement (date_paiement, montant_paye, mode_paiement, num_facture)
    VALUES (NOW(), p_montant_paye, p_mode_paiement, p_num_facture);

    SET o_id_paiement   = LAST_INSERT_ID();
    SET o_reste_a_payer = fn_reste_a_payer(p_num_facture);

    CALL sp_journaliser_action(
        p_id_utilisateur,
        CONCAT('Encaissement ', p_montant_paye, ' FCFA (', p_mode_paiement, ') sur ', p_num_facture),
        'COMMANDE',
        p_num_facture
    );
END$$

-- ----------------------------------------------------------------------------
-- 3.6 sp_valider_retrait_commande : Opération 4 du MCT — Retrait intégral
--     Double condition stricte : statut_cmd = « Prêt » ET reste_a_payer = 0 (RG6, RG16, CI6)
--     Clôture à « Retiré », historise chaque article et crédite les points fidélité (RG27)
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_valider_retrait_commande(
    IN p_num_facture    VARCHAR(14),
    IN p_id_utilisateur INT
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_statut_cmd    VARCHAR(20);
    DECLARE v_montant_total DECIMAL(10, 2);
    DECLARE v_id_client     INT;
    DECLARE v_reste         DECIMAL(10, 2);
    DECLARE v_ratio_txt     VARCHAR(255);
    DECLARE v_tranche_fcfa  DECIMAL(10, 2) DEFAULT 1000.00;
    DECLARE v_points_gagnes INT DEFAULT 0;

    SELECT statut_cmd, montant_total, id_client
    INTO v_statut_cmd, v_montant_total, v_id_client
    FROM commande
    WHERE num_facture = p_num_facture
    FOR UPDATE;

    IF v_statut_cmd IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Commande introuvable.';
    END IF;

    -- RG6 / CI6 : Condition 1 — Commande entièrement « Prête »
    IF v_statut_cmd <> 'Prêt' OR fn_commande_tous_articles_prets(p_num_facture) = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG6 / CI6 : Retrait refusé — tous les articles de la commande ne sont pas encore au statut « Prêt » (aucune restitution partielle autorisée).';
    END IF;

    -- RG16 / CI6 : Condition 2 — Commande intégralement soldée
    SET v_reste = fn_reste_a_payer(p_num_facture);
    IF v_reste > 0.00 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RG16 / CI6 : Retrait refusé — la commande n''est pas intégralement soldée. Veuillez encaisser le solde restant.';
    END IF;

    -- RG25 : Tracer le passage Prêt -> Retiré dans historique_statut pour tous les articles
    INSERT INTO historique_statut (ancien_statut, nouveau_statut, date_heure_statut, id_article, id_utilisateur)
    SELECT statut_article, 'Retiré', NOW(), id_article, p_id_utilisateur
    FROM article
    WHERE num_facture = p_num_facture;

    UPDATE article
    SET statut_article = 'Retiré'
    WHERE num_facture = p_num_facture;

    UPDATE commande
    SET statut_cmd = 'Retiré'
    WHERE num_facture = p_num_facture;

    -- RG27 : Créditation des points de fidélité selon points_par_montant (ex: 1 point par 1000 FCFA)
    SELECT valeur_param INTO v_ratio_txt
    FROM parametre
    WHERE code_param = 'points_par_montant'
    LIMIT 1;

    IF v_ratio_txt IS NOT NULL AND CAST(v_ratio_txt AS DECIMAL(10, 2)) > 0 THEN
        SET v_tranche_fcfa = CAST(v_ratio_txt AS DECIMAL(10, 2));
        SET v_points_gagnes = FLOOR(v_montant_total / v_tranche_fcfa);
        IF v_points_gagnes > 0 THEN
            UPDATE client
            SET points_fidelite = points_fidelite + v_points_gagnes
            WHERE id_client = v_id_client;
        END IF;
    END IF;

    CALL sp_journaliser_action(
        p_id_utilisateur,
        CONCAT('Retrait intégral validé et clôturé pour ', p_num_facture, ' (+', v_points_gagnes, ' pts)'),
        'COMMANDE',
        p_num_facture
    );
END$$

-- ----------------------------------------------------------------------------
-- 3.7 sp_rapport_pilotage_dg : Opération 6 du MCT — Reporting consolidé DG (BF8)
--     Présente côte à côte le CA facturé et le CA encaissé par site + volumes
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_rapport_pilotage_dg(
    IN p_date_debut DATE,
    IN p_date_fin   DATE
)
READS SQL DATA
BEGIN
    SELECT
        ag.id_agence,
        ag.code_agence,
        ag.nom_agence,
        ag.type_agence,
        COALESCE(cmd_stats.nb_commandes, 0)         AS nb_commandes,
        COALESCE(art_stats.nb_articles_traites, 0)  AS volume_vetements,
        COALESCE(cmd_stats.ca_facture, 0.00)        AS ca_facture_fcfa,
        COALESCE(pay_stats.ca_encaisse, 0.00)       AS ca_encaisse_fcfa,
        COALESCE(cmd_stats.ca_facture, 0.00) - COALESCE(pay_stats.ca_encaisse, 0.00) AS reste_a_recouvrer_fcfa,
        COALESCE(recl_stats.nb_reclamations, 0)     AS nb_reclamations_ouvertes
    FROM agence ag
    LEFT JOIN (
        SELECT
            id_agence,
            COUNT(*)           AS nb_commandes,
            SUM(montant_total) AS ca_facture
        FROM commande
        WHERE date_depot BETWEEN p_date_debut AND p_date_fin
        GROUP BY id_agence
    ) cmd_stats ON cmd_stats.id_agence = ag.id_agence
    LEFT JOIN (
        SELECT
            c.id_agence,
            COUNT(a.id_article) AS nb_articles_traites
        FROM article a
        INNER JOIN commande c ON c.num_facture = a.num_facture
        WHERE c.date_depot BETWEEN p_date_debut AND p_date_fin
        GROUP BY c.id_agence
    ) art_stats ON art_stats.id_agence = ag.id_agence
    LEFT JOIN (
        SELECT
            c.id_agence,
            SUM(p.montant_paye) AS ca_encaisse
        FROM paiement p
        INNER JOIN commande c ON c.num_facture = p.num_facture
        WHERE DATE(p.date_paiement) BETWEEN p_date_debut AND p_date_fin
        GROUP BY c.id_agence
    ) pay_stats ON pay_stats.id_agence = ag.id_agence
    LEFT JOIN (
        SELECT
            c.id_agence,
            COUNT(r.id_reclamation) AS nb_reclamations
        FROM reclamation r
        INNER JOIN commande c ON c.num_facture = r.num_facture
        WHERE r.statut_reclamation <> 'Résolue'
        GROUP BY c.id_agence
    ) recl_stats ON recl_stats.id_agence = ag.id_agence
    ORDER BY ag.type_agence ASC, ag.code_agence ASC;
END$$

DELIMITER ;
