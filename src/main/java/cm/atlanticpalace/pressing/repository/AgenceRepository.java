package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Agence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agence, Integer> {
    Optional<Agence> findByCodeAgence(String codeAgence);
}
