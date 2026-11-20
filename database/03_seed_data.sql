-- ============================================================================
-- ATLANTIC PALACE PRESSING — SYSTÈME CENTRALISÉ MULTI-AGENCES (v3.0)
-- Fichier 03 : Données initiales (6 agences de Yaoundé + Atelier, Paramètres,
--              Grille tarifaire FCFA, Comptes de démonstration et Commandes)
-- ============================================================================

SET NAMES utf8mb4;
USE atlantic_palace_pressing;

-- ============================================================================
-- 1. SITES : 6 AGENCES COMMERCIALES À YAOUNDÉ + 1 ATELIER CENTRAL (§1.2, §3.3)
-- ============================================================================
INSERT INTO agence (code_agence, nom_agence, type_agence, adresse_agence, tel_agence) VALUES
('YA1', 'Agence Bastos',        'Agence commerciale', 'Avenue Jean-Paul II, Bastos, Yaoundé',       '+237699000101'),
('YA2', 'Agence Kennedy',       'Agence commerciale', 'Avenue Kennedy, Centre-Ville, Yaoundé',      '+237699000102'),
('YA3', 'Agence Biyem-Assi',    'Agence commerciale', 'Rond-point Express, Biyem-Assi, Yaoundé',    '+237699000103'),
('YA4', 'Agence Essos',         'Agence commerciale', 'Camp Sonel, Essos, Yaoundé',                 '+237699000104'),
('YA5', 'Agence Omnisports',    'Agence commerciale', 'Face Stade Ahmadou Ahidjo, Yaoundé',         '+237699000105'),
('YA6', 'Agence Odza',          'Agence commerciale', 'Borne 10, Route de Mbalmayo, Odza, Yaoundé', '+237699000106'),
('ATL', 'Atelier Central',      'Atelier central',    'Zone Industrielle Mvan, Yaoundé',            '+237699000100');

-- ============================================================================
-- 2. COMPTES UTILISATEURS NOMINATIFS (RG24, BNF10)
-- Empreinte BCrypt initiale synchronisée au démarrage Spring Boot (mot de passe démo : Pressing@2026)
-- ============================================================================
INSERT INTO utilisateur (nom_utilisateur, login_utilisateur, mot_de_passe_hash, profil, statut_compte, id_agence) VALUES
('Direction Générale Siège',    'dg.admin',         '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'DG',             'Actif', NULL),
('Clarisse Mballa (Bastos)',    'recep.bastos',     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Réceptionniste', 'Actif', 1),
('Nadine Nguema (Kennedy)',     'recep.kennedy',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Réceptionniste', 'Actif', 2),
('Sandrine Owona (Biyem-Assi)', 'recep.biyem',      '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Réceptionniste', 'Actif', 3),
('Paul Kamga (Atelier Lavage)', 'agent.kamga',      '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Agent',          'Actif', 7),
('Éric Tchoumi (Repassage)',    'agent.tchoumi',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Agent',          'Actif', 7),
('Brice Fotso (Livreur v2)',    'livreur.fotso',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Livreur',        'Désactivé', 7);

-- ============================================================================
-- 3. PARAMÈTRES SYSTÈME GÉRÉS PAR LE DG (Tableau 30 — RG18, CI11)
-- ============================================================================
INSERT INTO parametre (categorie_param, code_param, libelle_param, valeur_param, date_maj_param, id_utilisateur) VALUES
('Remise',      'plafond_remise',       'Remise maximale accordable au comptoir (%)',               '10',                                                      NOW(), 1),
('Paiement',    'modes_paiement',       'Modes de règlement acceptés en caisse',                    'Espèces,Mobile Money,Carte',                              NOW(), 1),
('Statut',      'statuts_production',   'Liste ordonnée des statuts du workflow (RG10)',            'Réceptionné,En lavage,En repassage,Prêt,Retiré',          NOW(), 1),
('SMS',         'modele_sms',           'Modèle du SMS automatique client (BF5)',                   'Atlantic Palace Pressing : votre commande N°{NUM} est prête au retrait à l''agence {AGENCE}.', NOW(), 1),
('SMS',         'nb_renvois_sms',       'Nombre maximal de renvois automatiques SMS (BNF9)',        '3',                                                       NOW(), 1),
('Facturation', 'format_bordereau',     'Format officiel de numérotation hors ligne (RG26)',        'AG-AA-NNNNNN',                                            NOW(), 1),
('Fidélité',    'points_par_montant',   'Tranche FCFA donnant droit à 1 point fidélité (RG27)',     '1000',                                                    NOW(), 1),
('Facturation', 'devise',               'Devise d''expression des montants',                        'FCFA',                                                    NOW(), 1),
('Facturation', 'taux_tva',             'Taux de TVA applicable en % (0 par défaut)',               '0',                                                       NOW(), 1),
('Clients',     'unicite_telephone',    'Unicité stricte du numéro de téléphone client (§7.2)',     'true',                                                    NOW(), 1);

-- ============================================================================
-- 4. PRESTATIONS ET GRILLE TARIFAIRE INITIALE EN FCFA (RG17, RG23)
-- ============================================================================
INSERT INTO prestation (libelle_prestation, duree_estimee) VALUES
('Costume 2 pièces (À sec)',     48),
('Chemise (Lavage + Repassage)', 24),
('Pantalon classique',           24),
('Robe de soirée (À sec)',       72),
('Ensemble traditionnel / Ndop', 48),
('Couette double (Lavage eau)',  48),
('Veste / Blazer (À sec)',       36),
('Repassage simple vêtement',    12);

INSERT INTO tarif (prix_unitaire, date_debut_validite, date_fin_validite, id_prestation) VALUES
(3500.00, '2026-01-01', NULL, 1),
(1000.00, '2026-01-01', NULL, 2),
(1200.00, '2026-01-01', NULL, 3),
(4500.00, '2026-01-01', NULL, 4),
(4000.00, '2026-01-01', NULL, 5),
(5000.00, '2026-01-01', NULL, 6),
(2500.00, '2026-01-01', NULL, 7),
(500.00,  '2026-01-01', NULL, 8);

-- ============================================================================
-- 5. CLIENTS DE DÉMONSTRATION (Normalisés automatiquement via trigger RG9)
-- ============================================================================
INSERT INTO client (nom_client, tel_client, points_fidelite) VALUES
('Jean-Baptiste Atangana', '694112233', 12),
('Marie-Claire Ngo Biyong', '677445566', 5),
('Samuel Ekani', '655889900', 0);

-- ============================================================================
-- 6. JEU D'ESSAI VIA LES PROCÉDURES STOCKÉES (Dépôt + Articles + Acompte)
-- ============================================================================
CALL sp_creer_commande(1, 1, 2, 0.00, NULL, NULL, NULL, @cmd1);
CALL sp_ajouter_article_commande(@cmd1, 1, 'Légère tache revers droit constatée devant le client', 2, @art1);
CALL sp_ajouter_article_commande(@cmd1, 2, 'RAS — bouton manchette vérifié', 2, @art2);
CALL sp_enregistrer_paiement(@cmd1, 2500.00, 'Espèces', 2, @pay1, @reste1);

CALL sp_creer_commande(2, 2, 3, 5.00, NULL, NULL, NULL, @cmd2);
CALL sp_ajouter_article_commande(@cmd2, 4, 'Ourlet délicat avec perles', 3, @art3);
CALL sp_enregistrer_paiement(@cmd2, 4275.00, 'Mobile Money', 3, @pay2, @reste2);
