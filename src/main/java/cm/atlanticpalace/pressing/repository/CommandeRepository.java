package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, String> {
    List<Commande> findTop50ByAgence_IdAgenceOrderByDateDepotDesc(Integer idAgence);
    List<Commande> findTop50ByOrderByDateDepotDesc();
    List<Commande> findByStatutCmdInOrderByDateDepotAsc(List<String> statuts);
    List<Commande> findByStatutCmdOrderByDateDepotDesc(String statutCmd);
    List<Commande> findByAgence_IdAgenceAndStatutCmdOrderByDateDepotDesc(Integer idAgence, String statutCmd);
    List<Commande> findByClient_IdClientOrderByDateDepotDesc(Integer idClient);
    List<Commande> findByClient_NomClientIgnoreCaseOrderByDateDepotDesc(String nomClient);
    List<Commande> findByClient_TelClientOrderByDateDepotDesc(String telClient);

    @Query(value = "SELECT fn_reste_a_payer(:numFacture)", nativeQuery = true)
    java.math.BigDecimal calculerResteAPayer(@Param("numFacture") String numFacture);
}
