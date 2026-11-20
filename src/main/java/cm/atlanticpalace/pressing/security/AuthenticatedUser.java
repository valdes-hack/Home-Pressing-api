package cm.atlanticpalace.pressing.security;

import cm.atlanticpalace.pressing.domain.entity.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements UserDetails {

    private final Integer idUtilisateur;
    private final String nomUtilisateur;
    private final String loginUtilisateur;
    private final String motDePasseHash;
    private final String profil;
    private final String statutCompte;
    private final Integer idAgence;
    private final String codeAgence;
    private final String nomAgence;

    public AuthenticatedUser(Utilisateur u) {
        this.idUtilisateur = u.getIdUtilisateur();
        this.nomUtilisateur = u.getNomUtilisateur();
        this.loginUtilisateur = u.getLoginUtilisateur();
        this.motDePasseHash = u.getMotDePasseHash();
        this.profil = u.getProfil();
        this.statutCompte = u.getStatutCompte();
        this.idAgence = u.getAgence() != null ? u.getAgence().getIdAgence() : null;
        this.codeAgence = u.getAgence() != null ? u.getAgence().getCodeAgence() : null;
        this.nomAgence = u.getAgence() != null ? u.getAgence().getNomAgence() : "Siège — Direction Générale";
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public String getLoginUtilisateur() {
        return loginUtilisateur;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public String getProfil() {
        return profil;
    }

    public String getStatutCompte() {
        return statutCompte;
    }

    public Integer getIdAgence() {
        return idAgence;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public boolean isDg() {
        return "DG".equalsIgnoreCase(profil);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + profil.toUpperCase().replace("É", "E")));
    }

    @Override
    public String getPassword() {
        return motDePasseHash;
    }

    @Override
    public String getUsername() {
        return loginUtilisateur;
    }

    @Override
    public boolean isEnabled() {
        // CI17 / RG24 : un compte Désactivé ne peut pas ouvrir de session
        return "Actif".equalsIgnoreCase(statutCompte);
    }
}
