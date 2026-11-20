package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.entity.Agence;
import cm.atlanticpalace.pressing.domain.entity.Client;
import cm.atlanticpalace.pressing.domain.entity.Utilisateur;
import cm.atlanticpalace.pressing.repository.AgenceRepository;
import cm.atlanticpalace.pressing.repository.ClientRepository;
import cm.atlanticpalace.pressing.repository.UtilisateurRepository;
import cm.atlanticpalace.pressing.security.AuthenticatedUser;
import cm.atlanticpalace.pressing.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final AgenceRepository agenceRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UtilisateurRepository utilisateurRepository,
            ClientRepository clientRepository,
            AgenceRepository agenceRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.utilisateurRepository = utilisateurRepository;
        this.clientRepository = clientRepository;
        this.agenceRepository = agenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record LoginRequest(
            @NotBlank(message = "Login obligatoire") String login,
            @NotBlank(message = "Mot de passe obligatoire") String motDePasse
    ) {}

    public record RegisterRequest(
            @NotBlank(message = "Nom complet obligatoire") String nomUtilisateur,
            @NotBlank(message = "Identifiant login obligatoire") String login,
            @NotBlank(message = "Mot de passe obligatoire") String motDePasse,
            @NotBlank(message = "Numéro de téléphone obligatoire") String telephone,
            Integer idAgence
    ) {}

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "application", "Atlantic Palace Pressing API",
                "version", "3.0.0",
                "status", "UP"
        );
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login().trim(), request.motDePasse())
        );
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        return Map.of(
                "token", token,
                "utilisateur", toUserPayload(user)
        );
    }

    @PostMapping("/auth/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        String cleanLogin = request.login().trim().toLowerCase();

        // 1. Vérifier l'unicité du login
        if (utilisateurRepository.findByLoginUtilisateur(cleanLogin).isPresent()) {
            throw new IllegalArgumentException("Cet identifiant de connexion est déjà utilisé.");
        }

        // 2. Normalisation du numéro de téléphone camerounais (RG9)
        String rawTel = request.telephone().replaceAll("[^0-9+]", "").trim();
        String telNormalise;
        if (rawTel.startsWith("+237")) {
            telNormalise = rawTel;
        } else if (rawTel.startsWith("237")) {
            telNormalise = "+" + rawTel;
        } else if (rawTel.length() == 9 && (rawTel.startsWith("6") || rawTel.startsWith("2"))) {
            telNormalise = "+237" + rawTel;
        } else {
            telNormalise = rawTel.startsWith("+") ? rawTel : "+237" + rawTel;
        }

        // 3. Créer ou retrouver le client correspondant dans la table client
        Client client = clientRepository.findByTelClient(telNormalise)
                .orElseGet(() -> {
                    Client c = new Client();
                    c.setNomClient(request.nomUtilisateur().trim());
                    c.setTelClient(telNormalise);
                    c.setPointsFidelite(0);
                    return clientRepository.save(c);
                });

        // 4. Déterminer l'agence d'affectation
        Integer agenceId = request.idAgence() != null ? request.idAgence() : 1;
        Agence agence = agenceRepository.findById(agenceId)
                .orElseGet(() -> agenceRepository.findAll().stream().findFirst().orElse(null));

        // 5. Créer l'utilisateur avec rôle "Client" par défaut (CI17 / RG24)
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setNomUtilisateur(request.nomUtilisateur().trim());
        nouvelUtilisateur.setLoginUtilisateur(cleanLogin);
        nouvelUtilisateur.setMotDePasseHash(passwordEncoder.encode(request.motDePasse()));
        nouvelUtilisateur.setProfil("Client");
        nouvelUtilisateur.setStatutCompte("Actif");
        nouvelUtilisateur.setAgence(agence);

        Utilisateur savedUser = utilisateurRepository.save(nouvelUtilisateur);

        // 6. Authentification automatique & génération JWT
        AuthenticatedUser authUser = new AuthenticatedUser(savedUser);
        String token = jwtService.generateToken(authUser);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("utilisateur", toUserPayload(authUser));
        response.put("client", Map.of(
                "idClient", client.getIdClient(),
                "nomClient", client.getNomClient(),
                "telClient", client.getTelClient(),
                "pointsFidelite", client.getPointsFidelite()
        ));
        response.put("message", "Inscription réussie ! Bienvenue sur Atlantic Palace Pressing.");

        return response;
    }

    @GetMapping("/auth/me")
    public Map<String, Object> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return toUserPayload(user);
    }

    private Map<String, Object> toUserPayload(AuthenticatedUser user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("idUtilisateur", user.getIdUtilisateur() != null ? user.getIdUtilisateur() : 0);
        payload.put("nomUtilisateur", user.getNomUtilisateur() != null ? user.getNomUtilisateur() : "Utilisateur");
        payload.put("loginUtilisateur", user.getUsername() != null ? user.getUsername() : "");
        payload.put("profil", user.getProfil() != null ? user.getProfil() : "Client");
        payload.put("idAgence", user.getIdAgence() != null ? user.getIdAgence() : 0);
        payload.put("codeAgence", user.getCodeAgence() != null ? user.getCodeAgence() : "DG");
        payload.put("nomAgence", user.getNomAgence() != null ? user.getNomAgence() : "Siège");
        return payload;
    }
}
