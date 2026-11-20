package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Integer> {
    List<Reclamation> findTop50ByOrderByDateReclamationDesc();
    List<Reclamation> findByCommande_Agence_IdAgenceOrderByDateReclamationDesc(Integer idAgence);
}
