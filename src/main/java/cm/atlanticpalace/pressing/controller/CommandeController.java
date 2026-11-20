package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.entity.Article;
import cm.atlanticpalace.pressing.domain.entity.Commande;
import cm.atlanticpalace.pressing.domain.entity.HistoriqueStatut;
import cm.atlanticpalace.pressing.domain.entity.Paiement;
import cm.atlanticpalace.pressing.repository.*;
import cm.atlanticpalace.pressing.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/commandes")
public class CommandeController {

    private final CommandeRepository commandeRepository;
    private final ArticleRepository articleRepository;
    private final PaiementRepository paiementRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final PressingStoredProcedureRepository spRepository;

    public CommandeController(
            CommandeRepository commandeRepository,
            ArticleRepository articleRepository,
            PaiementRepository paiementRepository,
            HistoriqueStatutRepository historiqueStatutRepository,
            PressingStoredProcedureRepository spRepository
    ) {
        this.commandeRepository = commandeRepository;
        this.articleRepository = articleRepository;
        this.paiementRepository = paiementRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.spRepository = spRepository;
    }

    public record CreerCommandeRequest(
            @NotNull Integer idClient,
            BigDecimal tauxRemise,
            String numFactureSync, // v1 / BNF11 : si généré en mode hors ligne
            Integer idAbonnement,  // v2
            Integer idBon,         // v2
            List<ArticleItemRequest> articles,
            BigDecimal acompteInitial,
            String modePaiement
    ) {}

    public record ArticleItemRequest(
            @NotNull Integer idPrestation,
            String etatReception
    ) {}

    public record AjouterArticleRequest(
            @NotNull Integer idPrestation,
            String etatReception
    ) {}

    @GetMapping
    public List<Commande> listerCommandes(
            @RequestParam(value = "statut", required = false) String statut,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        // Espace Client : retourne uniquement les commandes du client connecté
        if (user.getProfil() != null && user.getProfil().equalsIgnoreCase("Client")) {
            return commandeRepository.findByClient_NomClientIgnoreCaseOrderByDateDepotDesc(user.getNomUtilisateur());
        }

        // RG15 : Cloisonnement par site (sauf DG qui a la vue globale)
        if (statut != null && !statut.isBlank() && !statut.equalsIgnoreCase("Tous")) {
            if (user.isDg()) {
                return commandeRepository.findByStatutCmdOrderByDateDepotDesc(statut);
            }
            return commandeRepository.findByAgence_IdAgenceAndStatutCmdOrderByDateDepotDesc(user.getIdAgence(), statut);
        }
        if (user.isDg()) {
            return commandeRepository.findTop50ByOrderByDateDepotDesc();
        }
        return commandeRepository.findTop50ByAgence_IdAgenceOrderByDateDepotDesc(user.getIdAgence());
    }

    @GetMapping("/mes-commandes")
    public List<Commande> mesCommandesClient(@AuthenticationPrincipal AuthenticatedUser user) {
        return commandeRepository.findByClient_NomClientIgnoreCaseOrderByDateDepotDesc(user.getNomUtilisateur());
    }

    @GetMapping("/{numFacture}")
    public Map<String, Object> detailsCommande(
            @PathVariable String numFacture,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Commande cmd = commandeRepository.findById(numFacture)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable : " + numFacture));

        // RG15 : Vérification du cloisonnement d'accès
        if (user.getProfil() != null && user.getProfil().equalsIgnoreCase("Client")) {
            if (cmd.getClient() == null || !cmd.getClient().getNomClient().equalsIgnoreCase(user.getNomUtilisateur())) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à consulter la commande d'un autre client.");
            }
        } else if (!user.isDg() && (user.getIdAgence() == null || !user.getIdAgence().equals(cmd.getAgence().getIdAgence()))) {
            throw new AccessDeniedException("RG15 : Vous n'êtes pas autorisé à consulter une commande d'une autre agence.");
        }

        List<Article> articles = articleRepository.findByCommande_NumFactureOrderByIdArticleAsc(numFacture);
        List<Paiement> paiements = paiementRepository.findByCommande_NumFactureOrderByDatePaiementAsc(numFacture);
        List<HistoriqueStatut> historique = historiqueStatutRepository.findByArticle_Commande_NumFactureOrderByDateHeureStatutAsc(numFacture);
        BigDecimal resteAPayer = commandeRepository.calculerResteAPayer(numFacture);

        return Map.of(
                "commande", cmd,
                "articles", articles,
                "paiements", paiements,
                "historique", historique,
                "resteAPayer", resteAPayer != null ? resteAPayer : BigDecimal.ZERO
        );
    }

    @PostMapping
    @Transactional
    public Map<String, Object> creerCommande(
            @Valid @RequestBody CreerCommandeRequest req,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Integer idAgence = user.getIdAgence() != null ? user.getIdAgence() : 1;

        // 1. Appel de la procédure MySQL sp_creer_commande (Opération 1 du MCT)
        String numFacture = spRepository.creerCommande(
                req.idClient(),
                idAgence,
                user.getIdUtilisateur(),
                req.tauxRemise(),
                req.numFactureSync(),
                req.idAbonnement(),
                req.idBon()
        );

        // 2. Ajout des articles via sp_ajouter_article_commande (tarifs en vigueur + historique RG25)
        if (req.articles() != null) {
            for (ArticleItemRequest art : req.articles()) {
                spRepository.ajouterArticleCommande(
                        numFacture,
                        art.idPrestation(),
                        art.etatReception(),
                        user.getIdUtilisateur()
                );
            }
        }

        // 3. Encaissement d'un acompte initial si renseigné (RG16)
        BigDecimal reste = null;
        if (req.acompteInitial() != null && req.acompteInitial().compareTo(BigDecimal.ZERO) > 0) {
            String mode = (req.modePaiement() != null && !req.modePaiement().isBlank())
                    ? req.modePaiement() : "Espèces";
            reste = spRepository.enregistrerPaiement(
                    numFacture,
                    req.acompteInitial(),
                    mode,
                    user.getIdUtilisateur()
            );
        }

        return Map.of(
                "message", "Commande enregistrée avec succès",
                "numFacture", numFacture,
                "resteAPayer", reste != null ? reste : BigDecimal.ZERO
        );
    }

    @PostMapping("/{numFacture}/articles")
    @Transactional
    public Map<String, Object> ajouterArticle(
            @PathVariable String numFacture,
            @Valid @RequestBody AjouterArticleRequest req,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Integer idArticle = spRepository.ajouterArticleCommande(
                numFacture,
                req.idPrestation(),
                req.etatReception(),
                user.getIdUtilisateur()
        );

        return Map.of(
                "message", "Article ajouté au bordereau",
                "idArticle", idArticle,
                "numFacture", numFacture
        );
    }
}
