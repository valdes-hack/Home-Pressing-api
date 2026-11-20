package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.repository.PressingStoredProcedureRepository;
import cm.atlanticpalace.pressing.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/caisse")
@PreAuthorize("hasAnyRole('RECEPTIONNISTE', 'DG')")
public class CaisseController {

    private final PressingStoredProcedureRepository spRepository;

    public CaisseController(PressingStoredProcedureRepository spRepository) {
        this.spRepository = spRepository;
    }

    public record PaiementRequest(
            @NotNull @Positive(message = "Le montant doit être positif") BigDecimal montantPaye,
            @NotBlank(message = "Le mode de paiement est obligatoire") String modePaiement
    ) {}

    @PostMapping("/commandes/{numFacture}/paiements")
    @Transactional
    public Map<String, Object> enregistrerPaiement(
            @PathVariable String numFacture,
            @Valid @RequestBody PaiementRequest req,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        BigDecimal reste = spRepository.enregistrerPaiement(
                numFacture,
                req.montantPaye(),
                req.modePaiement(),
                user.getIdUtilisateur()
        );

        return Map.of(
                "message", "Paiement enregistré avec succès",
                "numFacture", numFacture,
                "montantPaye", req.montantPaye(),
                "resteAPayer", reste
        );
    }

    @PostMapping("/commandes/{numFacture}/retrait")
    @Transactional
    public Map<String, Object> validerRetrait(
            @PathVariable String numFacture,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        spRepository.validerRetraitCommande(numFacture, user.getIdUtilisateur());

        return Map.of(
                "message", "Retrait intégral validé avec succès. Commande clôturée.",
                "numFacture", numFacture,
                "statutCmd", "Retiré"
        );
    }
}
