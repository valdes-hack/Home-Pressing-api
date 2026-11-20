package cm.atlanticpalace.pressing.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Pont JDBC direct vers les procédures stockées et fonctions MySQL (02_functions_procedures_triggers.sql).
 */
@Repository
public class PressingStoredProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public PressingStoredProcedureRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String creerCommande(
            Integer idClient,
            Integer idAgence,
            Integer idUtilisateur,
            BigDecimal tauxRemise,
            String numFactureSync,
            Integer idAbonnement,
            Integer idBon
    ) {
        return jdbcTemplate.execute(
                "{CALL sp_creer_commande(?, ?, ?, ?, ?, ?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setInt(1, idClient);
                    cs.setInt(2, idAgence);
                    cs.setInt(3, idUtilisateur);
                    cs.setBigDecimal(4, tauxRemise != null ? tauxRemise : BigDecimal.ZERO);
                    if (numFactureSync != null && !numFactureSync.isBlank()) {
                        cs.setString(5, numFactureSync);
                    } else {
                        cs.setNull(5, Types.VARCHAR);
                    }
                    if (idAbonnement != null) {
                        cs.setInt(6, idAbonnement);
                    } else {
                        cs.setNull(6, Types.INTEGER);
                    }
                    if (idBon != null) {
                        cs.setInt(7, idBon);
                    } else {
                        cs.setNull(7, Types.INTEGER);
                    }
                    cs.registerOutParameter(8, Types.VARCHAR);
                    cs.execute();
                    return cs.getString(8);
                }
        );
    }

    public Integer ajouterArticleCommande(
            String numFacture,
            Integer idPrestation,
            String etatReception,
            Integer idUtilisateur
    ) {
        return jdbcTemplate.execute(
                "{CALL sp_ajouter_article_commande(?, ?, ?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setString(1, numFacture);
                    cs.setInt(2, idPrestation);
                    cs.setString(3, etatReception);
                    cs.setInt(4, idUtilisateur);
                    cs.registerOutParameter(5, Types.INTEGER);
                    cs.execute();
                    return cs.getInt(5);
                }
        );
    }

    public void changerStatutArticle(Integer idArticle, String nouveauStatut, Integer idUtilisateur) {
        jdbcTemplate.execute(
                "{CALL sp_changer_statut_article(?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setInt(1, idArticle);
                    cs.setString(2, nouveauStatut);
                    cs.setInt(3, idUtilisateur);
                    cs.execute();
                    return null;
                }
        );
    }

    public BigDecimal enregistrerPaiement(
            String numFacture,
            BigDecimal montantPaye,
            String modePaiement,
            Integer idUtilisateur
    ) {
        return jdbcTemplate.execute(
                "{CALL sp_enregistrer_paiement(?, ?, ?, ?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setString(1, numFacture);
                    cs.setBigDecimal(2, montantPaye);
                    cs.setString(3, modePaiement);
                    cs.setInt(4, idUtilisateur);
                    cs.registerOutParameter(5, Types.INTEGER);
                    cs.registerOutParameter(6, Types.DECIMAL);
                    cs.execute();
                    return cs.getBigDecimal(6);
                }
        );
    }

    public void validerRetraitCommande(String numFacture, Integer idUtilisateur) {
        jdbcTemplate.execute(
                "{CALL sp_valider_retrait_commande(?, ?)}",
                (CallableStatement cs) -> {
                    cs.setString(1, numFacture);
                    cs.setInt(2, idUtilisateur);
                    cs.execute();
                    return null;
                }
        );
    }

    public List<Map<String, Object>> rapportPilotageDg(LocalDate dateDebut, LocalDate dateFin) {
        return jdbcTemplate.queryForList(
                "CALL sp_rapport_pilotage_dg(?, ?)",
                Date.valueOf(dateDebut),
                Date.valueOf(dateFin)
        );
    }
}
