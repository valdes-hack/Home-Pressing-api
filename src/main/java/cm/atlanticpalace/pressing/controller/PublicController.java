package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.dto.CommandeTrackingDTO;
import cm.atlanticpalace.pressing.domain.dto.ContactDTO;
import cm.atlanticpalace.pressing.domain.entity.Article;
import cm.atlanticpalace.pressing.domain.entity.Commande;
import cm.atlanticpalace.pressing.domain.entity.ContactMessage;
import cm.atlanticpalace.pressing.repository.CommandeRepository;
import cm.atlanticpalace.pressing.repository.ContactMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur public pour l'application client (home-pressing).
 * Aucune authentification JWT requise pour ces endpoints.
 * Utilisé pour le suivi de commande et le formulaire de contact.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
@Validated
public class PublicController {

    private final CommandeRepository commandeRepository;
    private final ContactMessageRepository contactMessageRepository;

    public PublicController(
            CommandeRepository commandeRepository,
            ContactMessageRepository contactMessageRepository
    ) {
        this.commandeRepository = commandeRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * Suivi public de commande (sans authentification).
     * Endpoint utilisé par l'application client pour le tracking en temps réel.
     * 
     * GET /api/v1/commandes/{numFacture}/tracking
     * 
     * @param numFacture Numéro de bordereau au format AG-AA-NNNNNN (ex: YA1-26-000001)
     * @return DTO avec statut, progression, et infos publiques
     */
    @GetMapping("/commandes/{numFacture}/tracking")
    public ResponseEntity<CommandeTrackingDTO> trackCommande(
            @PathVariable String numFacture
    ) {
        Optional<Commande> opt = commandeRepository.findById(numFacture.toUpperCase().trim());

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Commande cmd = opt.get();

        // Construction du DTO public (sans informations sensibles prix/paiements)
        CommandeTrackingDTO dto = new CommandeTrackingDTO();
        dto.setNumFacture(cmd.getNumFacture());
        dto.setStatutCmd(cmd.getStatutCmd());
        dto.setDateDepot(cmd.getDateDepot());
        dto.setNomAgence(cmd.getAgence() != null ? cmd.getAgence().getNomAgence() : "Agence");
        dto.setCodeAgence(cmd.getAgence() != null ? cmd.getAgence().getCodeAgence() : "N/A");
        dto.setNbArticles(cmd.getArticles() != null ? cmd.getArticles().size() : 0);

        // Calcul de la progression (pourcentage d'articles prêts)
        if (cmd.getArticles() != null && !cmd.getArticles().isEmpty()) {
            long nbPrets = cmd.getArticles().stream()
                    .filter(a -> "Prêt".equalsIgnoreCase(a.getStatutArticle()) 
                              || "Retiré".equalsIgnoreCase(a.getStatutArticle())
                              || "Livré".equalsIgnoreCase(a.getStatutArticle()))
                    .count();
            dto.setProgression(Math.round((nbPrets * 100.0f) / cmd.getArticles().size()));
        } else {
            dto.setProgression(0);
        }

        // Message informatif selon le statut
        dto.setMessage(genererMessage(cmd.getStatutCmd()));

        return ResponseEntity.ok(dto);
    }

    /**
     * Soumission du formulaire de contact public.
     * Enregistre le message en base de données pour traitement par l'équipe.
     * 
     * POST /api/v1/public/contact
     * 
     * @param contactDTO DTO validé avec nom, email, téléphone, sujet, message
     * @return Confirmation de réception
     */
    @PostMapping("/public/contact")
    public ResponseEntity<Map<String, String>> soumettreContact(
            @RequestBody @Valid ContactDTO contactDTO
    ) {
        ContactMessage msg = new ContactMessage();
        msg.setNom(contactDTO.getNom().trim());
        msg.setEmail(contactDTO.getEmail().trim().toLowerCase());
        msg.setTelephone(contactDTO.getTelephone() != null ? contactDTO.getTelephone().trim() : null);
        msg.setSujet(contactDTO.getSujet().trim());
        msg.setMessage(contactDTO.getMessage().trim());
        msg.setDateReception(LocalDateTime.now());
        msg.setStatut("Non traité");

        contactMessageRepository.save(msg);

        return ResponseEntity.ok(Map.of(
                "message", "Message reçu avec succès ! Nous vous recontacterons dans les plus brefs délais.",
                "id", String.valueOf(msg.getIdMessage())
        ));
    }

    /**
     * Génère un message informatif selon le statut de la commande.
     */
    private String genererMessage(String statut) {
        if (statut == null) return "Commande en cours de traitement.";
        
        String s = statut.toLowerCase();
        if (s.contains("réceptionné") || s.contains("receptionne")) {
            return "Votre commande a été réceptionnée. Elle sera bientôt envoyée à l'atelier central.";
        }
        if (s.contains("lavage")) {
            return "Votre linge est actuellement en cours de lavage à l'atelier central.";
        }
        if (s.contains("repassage")) {
            return "Votre linge est en cours de repassage. Presque terminé !";
        }
        if (s.contains("prêt") || s.contains("pret")) {
            return "✅ Votre commande est prête ! Vous avez dû recevoir un SMS. Venez la récupérer à l'agence.";
        }
        if (s.contains("retiré") || s.contains("retire")) {
            return "Commande clôturée. Merci d'avoir fait confiance à Atlantic Palace Pressing !";
        }
        if (s.contains("livré") || s.contains("livre")) {
            return "Commande livrée avec succès. Merci pour votre confiance !";
        }
        return "Commande en cours de traitement.";
    }
}
