package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.entity.Agence;
import cm.atlanticpalace.pressing.domain.entity.Parametre;
import cm.atlanticpalace.pressing.domain.entity.Tarif;
import cm.atlanticpalace.pressing.domain.entity.Utilisateur;
import cm.atlanticpalace.pressing.repository.AgenceRepository;
import cm.atlanticpalace.pressing.repository.ParametreRepository;
import cm.atlanticpalace.pressing.repository.TarifRepository;
import cm.atlanticpalace.pressing.repository.UtilisateurRepository;
import cm.atlanticpalace.pressing.security.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/referentiel")
public class ReferentielController {

    private final AgenceRepository agenceRepository;
    private final TarifRepository tarifRepository;
    private final ParametreRepository parametreRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ReferentielController(
            AgenceRepository agenceRepository,
            TarifRepository tarifRepository,
            ParametreRepository parametreRepository,
            UtilisateurRepository utilisateurRepository
    ) {
        this.agenceRepository = agenceRepository;
        this.tarifRepository = tarifRepository;
        this.parametreRepository = parametreRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/agences")
    public List<Agence> listerAgences() {
        return agenceRepository.findAll();
    }

    @GetMapping("/tarifs-actifs")
    public List<Tarif> listerTarifsActifs() {
        return tarifRepository.findAllTarifsEnVigueur();
    }

    @GetMapping("/parametres")
    public List<Parametre> listerParametres() {
        return parametreRepository.findAll();
    }

    @PutMapping("/parametres/{codeParam}")
    @PreAuthorize("hasRole('DG')")
    public Parametre modifierParametre(
            @PathVariable String codeParam,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Parametre p = parametreRepository.findByCodeParam(codeParam)
                .orElseGet(() -> {
                    Parametre np = new Parametre();
                    np.setCodeParam(codeParam);
                    np.setLibelleParam(payload.getOrDefault("libelleParam", codeParam));
                    np.setCategorieParam(payload.getOrDefault("categorieParam", "Général"));
                    return np;
                });

        String nouvelleValeur = payload.get("valeurParam");
        if (nouvelleValeur != null) {
            p.setValeurParam(nouvelleValeur.trim());
            p.setDateMajParam(LocalDateTime.now());
            if (user != null && user.getIdUtilisateur() != null) {
                p.setUtilisateur(new Utilisateur(user.getIdUtilisateur()));
            }
            return parametreRepository.save(p);
        }
        return p;
    }

    @GetMapping("/utilisateurs")
    @PreAuthorize("hasRole('DG')")
    public List<Utilisateur> listerUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @PatchMapping("/utilisateurs/{idUtilisateur}/statut")
    @PreAuthorize("hasRole('DG')")
    public Utilisateur modifierStatutUtilisateur(
            @PathVariable Integer idUtilisateur,
            @RequestBody Map<String, String> payload
    ) {
        Utilisateur u = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + idUtilisateur));

        String nouveauStatut = payload.get("statutCompte");
        if (nouveauStatut != null && (nouveauStatut.equalsIgnoreCase("Actif") || nouveauStatut.equalsIgnoreCase("Désactivé"))) {
            u.setStatutCompte(nouveauStatut.substring(0, 1).toUpperCase() + nouveauStatut.substring(1).toLowerCase());
            return utilisateurRepository.save(u);
        } else {
            throw new IllegalArgumentException("Statut invalide. Utilisez 'Actif' ou 'Désactivé'.");
        }
    }
}
