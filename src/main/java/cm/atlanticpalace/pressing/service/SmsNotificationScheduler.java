package cm.atlanticpalace.pressing.service;

import cm.atlanticpalace.pressing.domain.entity.Notification;
import cm.atlanticpalace.pressing.domain.entity.Parametre;
import cm.atlanticpalace.pressing.repository.NotificationRepository;
import cm.atlanticpalace.pressing.repository.ParametreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service automatique d'envoi et de renvoi des notifications SMS (BF5, BNF9, RG30).
 */
@Service
public class SmsNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final ParametreRepository parametreRepository;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    public SmsNotificationScheduler(
            NotificationRepository notificationRepository,
            ParametreRepository parametreRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.parametreRepository = parametreRepository;
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void traiterFileSms() {
        int maxRenvois = parametreRepository.findByCodeParam("nb_renvois_sms")
                .map(Parametre::getValeurParam)
                .map(Integer::parseInt)
                .orElse(3);

        String modeleSms = parametreRepository.findByCodeParam("modele_sms")
                .map(Parametre::getValeurParam)
                .orElse("Atlantic Palace Pressing : votre commande N°{NUM} est prête au retrait.");

        List<Notification> enAttente = notificationRepository
                .findByStatutSmsInAndNbRenvoisLessThanOrderByIdNotificationAsc(
                        List.of("En attente", "Échec"),
                        maxRenvois
                );

        for (Notification notif : enAttente) {
            String numFacture = notif.getCommande().getNumFacture();
            String telClient = notif.getCommande().getClient().getTelClient();
            String nomAgence = notif.getCommande().getAgence().getNomAgence();
            String message = modeleSms
                    .replace("{NUM}", numFacture)
                    .replace("{AGENCE}", nomAgence);

            try {
                if (smsEnabled) {
                    log.info("[SMS PASSERELLE] Envoi vers {} : {}", telClient, message);
                } else {
                    log.info("[SMS SIMULATION BF5] Vers {} : {}", telClient, message);
                }
                notif.setStatutSms("Envoyé");
                notif.setDateEnvoiSms(LocalDateTime.now());
            } catch (Exception ex) {
                notif.setStatutSms("Échec");
                notif.setNbRenvois(notif.getNbRenvois() + 1);
                notif.setDateEnvoiSms(LocalDateTime.now());
                log.warn("[SMS ÉCHEC F14] Commande {} (tentative {}/{}) : {}",
                        numFacture, notif.getNbRenvois(), maxRenvois, ex.getMessage());
            }
            notificationRepository.save(notif);
        }
    }
}
