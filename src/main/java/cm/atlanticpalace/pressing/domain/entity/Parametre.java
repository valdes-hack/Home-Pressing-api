package cm.atlanticpalace.pressing.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parametre")
public class Parametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametre")
    private Integer idParametre;

    @Column(name = "categorie_param", length = 20, nullable = false)
    private String categorieParam;

    @Column(name = "code_param", length = 30, nullable = false, unique = true)
    private String codeParam;

    @Column(name = "libelle_param", length = 100, nullable = false)
    private String libelleParam;

    @Column(name = "valeur_param", length = 255, nullable = false)
    private String valeurParam;

    @Column(name = "date_maj_param", nullable = false)
    private LocalDateTime dateMajParam;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    public Parametre() {}

    public Integer getIdParametre() { return idParametre; }
    public void setIdParametre(Integer idParametre) { this.idParametre = idParametre; }

    public String getCategorieParam() { return categorieParam; }
    public void setCategorieParam(String categorieParam) { this.categorieParam = categorieParam; }

    public String getCodeParam() { return codeParam; }
    public void setCodeParam(String codeParam) { this.codeParam = codeParam; }

    public String getLibelleParam() { return libelleParam; }
    public void setLibelleParam(String libelleParam) { this.libelleParam = libelleParam; }

    public String getValeurParam() { return valeurParam; }
    public void setValeurParam(String valeurParam) { this.valeurParam = valeurParam; }

    public LocalDateTime getDateMajParam() { return dateMajParam; }
    public void setDateMajParam(LocalDateTime dateMajParam) { this.dateMajParam = dateMajParam; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
