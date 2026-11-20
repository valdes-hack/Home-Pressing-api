package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.entity.Article;
import cm.atlanticpalace.pressing.domain.entity.Commande;
import cm.atlanticpalace.pressing.repository.ArticleRepository;
import cm.atlanticpalace.pressing.repository.CommandeRepository;
import cm.atlanticpalace.pressing.repository.PressingStoredProcedureRepository;
import cm.atlanticpalace.pressing.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/atelier")
@PreAuthorize("hasAnyRole('AGENT', 'DG')")
public class AtelierController {

    private final CommandeRepository commandeRepository;
    private final ArticleRepository articleRepository;
    private final PressingStoredProcedureRepository spRepository;

    public AtelierController(
            CommandeRepository commandeRepository,
            ArticleRepository articleRepository,
            PressingStoredProcedureRepository spRepository
    ) {
        this.commandeRepository = commandeRepository;
        this.articleRepository = articleRepository;
        this.spRepository = spRepository;
    }

    public record ChangementStatutRequest(
            @NotBlank(message = "Le nouveau statut est obligatoire") String nouveauStatut
    ) {}

    @GetMapping("/file-attente")
    public List<Commande> fileAttenteProduction() {
        return commandeRepository.findByStatutCmdInOrderByDateDepotAsc(
                List.of("Réceptionné", "En lavage", "En repassage")
        );
    }

    @GetMapping("/scan/{numFacture}")
    public Map<String, Object> scannerBordereau(@PathVariable String numFacture) {
        Commande cmd = commandeRepository.findById(numFacture)
                .orElseThrow(() -> new IllegalArgumentException("Numéro de bordereau introuvable : " + numFacture));

        List<Article> articles = articleRepository.findByCommande_NumFactureOrderByIdArticleAsc(numFacture);

        return Map.of(
                "commande", cmd,
                "articles", articles
        );
    }

    @PatchMapping("/articles/{idArticle}/statut")
    @Transactional
    public Map<String, Object> changerStatutArticle(
            @PathVariable Integer idArticle,
            @Valid @RequestBody ChangementStatutRequest req,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        spRepository.changerStatutArticle(
                idArticle,
                req.nouveauStatut(),
                user.getIdUtilisateur()
        );

        Article article = articleRepository.findById(idArticle)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable : " + idArticle));

        return Map.of(
                "message", "Statut article mis à jour avec succès",
                "idArticle", idArticle,
                "statutArticle", article.getStatutArticle(),
                "statutCommande", article.getCommande().getStatutCmd()
        );
    }
}
