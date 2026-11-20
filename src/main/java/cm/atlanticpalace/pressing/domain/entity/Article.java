package cm.atlanticpalace.pressing.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_article")
    private Integer idArticle;

    @Column(name = "etat_reception", length = 100)
    private String etatReception;

    @Column(name = "prix_article", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixArticle;

    @Column(name = "statut_article", length = 20, nullable = false)
    private String statutArticle;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "num_facture", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_prestation", nullable = false)
    private Prestation prestation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tarif", nullable = false)
    private Tarif tarif;

    public Article() {}

    public Integer getIdArticle() { return idArticle; }
    public void setIdArticle(Integer idArticle) { this.idArticle = idArticle; }

    public String getEtatReception() { return etatReception; }
    public void setEtatReception(String etatReception) { this.etatReception = etatReception; }

    public BigDecimal getPrixArticle() { return prixArticle; }
    public void setPrixArticle(BigDecimal prixArticle) { this.prixArticle = prixArticle; }

    public String getStatutArticle() { return statutArticle; }
    public void setStatutArticle(String statutArticle) { this.statutArticle = statutArticle; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Prestation getPrestation() { return prestation; }
    public void setPrestation(Prestation prestation) { this.prestation = prestation; }

    public Tarif getTarif() { return tarif; }
    public void setTarif(Tarif tarif) { this.tarif = tarif; }
}
