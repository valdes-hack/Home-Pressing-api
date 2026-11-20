package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bon_reduction")
public class BonReduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bon")
    private Integer idBon;

    @Column(name = "code_bon", length = 12, nullable = false, unique = true)
    private String codeBon;

    @Column(name = "valeur_bon", precision = 10, scale = 2, nullable = false)
    private BigDecimal valeurBon;

    @Column(name = "date_debut_bon", nullable = false)
    private LocalDate dateDebutBon;

    @Column(name = "date_fin_bon", nullable = false)
    private LocalDate dateFinBon;

    @Column(name = "statut_bon", length = 10, nullable = false)
    private String statutBon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    public BonReduction() {}

    public Integer getIdBon() { return idBon; }
    public void setIdBon(Integer idBon) { this.idBon = idBon; }

    public String getCodeBon() { return codeBon; }
    public void setCodeBon(String codeBon) { this.codeBon = codeBon; }

    public BigDecimal getValeurBon() { return valeurBon; }
    public void setValeurBon(BigDecimal valeurBon) { this.valeurBon = valeurBon; }

    public LocalDate getDateDebutBon() { return dateDebutBon; }
    public void setDateDebutBon(LocalDate dateDebutBon) { this.dateDebutBon = dateDebutBon; }

    public LocalDate getDateFinBon() { return dateFinBon; }
    public void setDateFinBon(LocalDate dateFinBon) { this.dateFinBon = dateFinBon; }

    public String getStatutBon() { return statutBon; }
    public void setStatutBon(String statutBon) { this.statutBon = statutBon; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
