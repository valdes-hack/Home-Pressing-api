package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Integer> {
    List<Paiement> findByCommande_NumFactureOrderByDatePaiementAsc(String numFacture);
}
