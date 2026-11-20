package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    Optional<Parametre> findByCodeParam(String codeParam);
}
