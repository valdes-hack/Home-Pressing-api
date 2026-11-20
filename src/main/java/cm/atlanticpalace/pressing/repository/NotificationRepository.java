package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByStatutSmsInAndNbRenvoisLessThanOrderByIdNotificationAsc(List<String> statuts, Integer maxRenvois);
    List<Notification> findByCommande_NumFactureOrderByIdNotificationDesc(String numFacture);
    List<Notification> findTop50ByOrderByIdNotificationDesc();
}
