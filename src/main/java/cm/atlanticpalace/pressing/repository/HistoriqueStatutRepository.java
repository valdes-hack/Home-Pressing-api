package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Integer> {
    List<HistoriqueStatut> findByArticle_Commande_NumFactureOrderByDateHeureStatutAsc(String numFacture);
}
