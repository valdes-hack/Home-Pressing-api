package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByLoginUtilisateur(String loginUtilisateur);
}
