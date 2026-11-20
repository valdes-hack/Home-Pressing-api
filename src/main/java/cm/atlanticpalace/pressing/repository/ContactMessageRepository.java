package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {
    
    /**
     * Trouve tous les messages par statut.
     */
    List<ContactMessage> findByStatut(String statut);
    
    /**
     * Trouve tous les messages triés par date de réception décroissante.
     */
    List<ContactMessage> findAllByOrderByDateReceptionDesc();
    
    /**
     * Trouve tous les messages non traités.
     */
    default List<ContactMessage> findMessagesNonTraites() {
        return findByStatut("Non traité");
    }
}
