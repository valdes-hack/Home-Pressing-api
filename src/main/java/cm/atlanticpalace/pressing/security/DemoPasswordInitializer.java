package cm.atlanticpalace.pressing.security;

import cm.atlanticpalace.pressing.domain.entity.Utilisateur;
import cm.atlanticpalace.pressing.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * S'assure au premier démarrage que les comptes de démonstration du seed SQL
 * disposent d'une empreinte BCrypt valide pour « Pressing@2026 » (RG24, BNF10).
 */
@Component
public class DemoPasswordInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoPasswordInitializer.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoPasswordInitializer(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String placeholder = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a";
        List<Utilisateur> users = utilisateurRepository.findAll();
        boolean updated = false;

        for (Utilisateur u : users) {
            if (placeholder.equals(u.getMotDePasseHash())) {
                u.setMotDePasseHash(passwordEncoder.encode("Pressing@2026"));
                utilisateurRepository.save(u);
                updated = true;
            }
        }

        if (updated) {
            log.info("Empreintes BCrypt des comptes de démonstration initialisées (mot de passe : Pressing@2026).");
        }
    }
}
