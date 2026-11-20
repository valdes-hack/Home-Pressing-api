package cm.atlanticpalace.pressing.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer idUtilisateur;

    @Column(name = "nom_utilisateur", length = 50, nullable = false)
    private String nomUtilisateur;

    @Column(name = "login_utilisateur", length = 30, nullable = false, unique = true)
    private String loginUtilisateur;

    @JsonIgnore
    @Column(name = "mot_de_passe_hash", length = 60, nullable = false)
    private String motDePasseHash;

    @Column(name = "profil", length = 15, nullable = false)
    private String profil;

    @Column(name = "statut_compte", length = 10, nullable = false)
    private String statutCompte;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_agence")
    private Agence agence;

    public Utilisateur() {}
    public Utilisateur(Integer idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public Integer getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Integer idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getLoginUtilisateur() { return loginUtilisateur; }
    public void setLoginUtilisateur(String loginUtilisateur) { this.loginUtilisateur = loginUtilisateur; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }

    public String getProfil() { return profil; }
    public void setProfil(String profil) { this.profil = profil; }

    public String getStatutCompte() { return statutCompte; }
    public void setStatutCompte(String statutCompte) { this.statutCompte = statutCompte; }

    public Agence getAgence() { return agence; }
    public void setAgence(Agence agence) { this.agence = agence; }
}
