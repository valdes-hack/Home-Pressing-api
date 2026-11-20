package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TarifRepository extends JpaRepository<Tarif, Integer> {

    @Query("SELECT t FROM Tarif t WHERE t.dateDebutValidite <= CURRENT_DATE AND (t.dateFinValidite IS NULL OR t.dateFinValidite >= CURRENT_DATE) ORDER BY t.prestation.libellePrestation ASC")
    List<Tarif> findAllTarifsEnVigueur();
}
