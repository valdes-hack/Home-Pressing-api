package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "livraison")
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livraison")
    private Integer idLivraison;

    @Column(name = "adresse_livraison", length = 120, nullable = false)
    private String adresseLivraison;

    @Column(name = "statut_livraison", length = 12, nullable = false)
    private String statutLivraison;

    @Column(name = "date_souhaitee")
    private LocalDate dateSouhaitee;

    @Column(name = "frais_livraison", precision = 7, scale = 2, nullable = false)
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "num_facture", nullable = false, unique = true)
    private Commande commande;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    public Livraison() {}

    public Integer getIdLivraison() { return idLivraison; }
    public void setIdLivraison(Integer idLivraison) { this.idLivraison = idLivraison; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }

    public String getStatutLivraison() { return statutLivraison; }
    public void setStatutLivraison(String statutLivraison) { this.statutLivraison = statutLivraison; }

    public LocalDate getDateSouhaitee() { return dateSouhaitee; }
    public void setDateSouhaitee(LocalDate dateSouhaitee) { this.dateSouhaitee = dateSouhaitee; }

    public BigDecimal getFraisLivraison() { return fraisLivraison; }
    public void setFraisLivraison(BigDecimal fraisLivraison) { this.fraisLivraison = fraisLivraison; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
