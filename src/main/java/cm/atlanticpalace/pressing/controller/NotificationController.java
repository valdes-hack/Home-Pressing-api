package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.entity.Commande;
import cm.atlanticpalace.pressing.domain.entity.Notification;
import cm.atlanticpalace.pressing.domain.entity.Parametre;
import cm.atlanticpalace.pressing.repository.NotificationRepository;
import cm.atlanticpalace.pressing.repository.ParametreRepository;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final ParametreRepository parametreRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            ParametreRepository parametreRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.parametreRepository = parametreRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listerNotifications() {
        String modeleWa = parametreRepository.findByCodeParam("modele_whatsapp")
                .map(Parametre::getValeurParam)
                .orElse("Bonjour {CLIENT}, votre commande N°{NUM} est prête au retrait à l'agence {AGENCE}. Solde : {SOLDE} FCFA.");

        String modeleMail = parametreRepository.findByCodeParam("modele_email")
                .map(Parametre::getValeurParam)
                .orElse("Bonjour {CLIENT}, votre commande N°{NUM} est prête à l'agence {AGENCE}. Solde : {SOLDE} FCFA.");

        List<Notification> list = notificationRepository.findTop50ByOrderByIdNotificationDesc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Notification n : list) {
            Commande cmd = n.getCommande();
            String nomClient = cmd != null && cmd.getClient() != null ? cmd.getClient().getNomClient() : "Client";
            String telClient = cmd != null && cmd.getClient() != null ? cmd.getClient().getTelClient() : "";
            String nomAgence = cmd != null && cmd.getAgence() != null ? cmd.getAgence().getNomAgence() : "Agence";
            String numFacture = cmd != null ? cmd.getNumFacture() : "";
            String solde = cmd != null ? String.valueOf(cmd.getMontantTotal()) : "0";

            String msgWa = modeleWa
                    .replace("{CLIENT}", nomClient)
                    .replace("{NUM}", numFacture)
                    .replace("{AGENCE}", nomAgence)
                    .replace("{SOLDE}", solde)
                    .replace("{ARTICLES}", "Articles traités avec soin");

            String msgEmail = modeleMail
                    .replace("{CLIENT}", nomClient)
                    .replace("{NUM}", numFacture)
                    .replace("{AGENCE}", nomAgence)
                    .replace("{SOLDE}", solde);

            // Lien direct wa.me
            String telNettoye = telClient.replace("+", "").replace(" ", "").trim();
            String waLink = "https://wa.me/" + telNettoye + "?text=" + URLEncoder.encode(msgWa, StandardCharsets.UTF_8);
            String mailtoLink = "mailto:" + nomClient.toLowerCase().replaceAll("[^a-z0-9]", ".") + "@gmail.com"
                    + "?subject=" + URLEncoder.encode("Votre commande N° " + numFacture + " est prête - Atlantic Palace Pressing", StandardCharsets.UTF_8)
                    + "&body=" + URLEncoder.encode(msgEmail, StandardCharsets.UTF_8);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("idNotification", n.getIdNotification());
            map.put("dateEnvoiSms", n.getDateEnvoiSms());
            map.put("statutSms", n.getStatutSms());
            map.put("nbRenvois", n.getNbRenvois());
            map.put("numFacture", numFacture);
            map.put("nomClient", nomClient);
            map.put("telClient", telClient);
            map.put("nomAgence", nomAgence);
            map.put("messageWhatsApp", msgWa);
            map.put("messageEmail", msgEmail);
            map.put("waLink", waLink);
            map.put("mailtoLink", mailtoLink);

            result.add(map);
        }

        return result;
    }
}
