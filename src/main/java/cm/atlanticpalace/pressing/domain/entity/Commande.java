package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "commande")
public class Commande {

    @Id
    @Column(name = "num_facture", length = 14)
    private String numFacture;

    @Column(name = "date_depot", nullable = false)
    private LocalDate dateDepot;

    @Column(name = "statut_cmd", length = 20, nullable = false)
    private String statutCmd;

    @Column(name = "taux_remise", precision = 5, scale = 2, nullable = false)
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    @Column(name = "montant_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_agence", nullable = false)
    private Agence agence;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_abonnement")
    private Abonnement abonnement;

    @Column(name = "date_utilisation")
    private LocalDate dateUtilisation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bon")
    private BonReduction bonReduction;

    public Commande() {}

    public String getNumFacture() { return numFacture; }
    public void setNumFacture(String numFacture) { this.numFacture = numFacture; }

    public LocalDate getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDate dateDepot) { this.dateDepot = dateDepot; }

    public String getStatutCmd() { return statutCmd; }
    public void setStatutCmd(String statutCmd) { this.statutCmd = statutCmd; }

    public BigDecimal getTauxRemise() { return tauxRemise; }
    public void setTauxRemise(BigDecimal tauxRemise) { this.tauxRemise = tauxRemise; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Agence getAgence() { return agence; }
    public void setAgence(Agence agence) { this.agence = agence; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Abonnement getAbonnement() { return abonnement; }
    public void setAbonnement(Abonnement abonnement) { this.abonnement = abonnement; }

    public LocalDate getDateUtilisation() { return dateUtilisation; }
    public void setDateUtilisation(LocalDate dateUtilisation) { this.dateUtilisation = dateUtilisation; }

    public BonReduction getBonReduction() { return bonReduction; }
    public void setBonReduction(BonReduction bonReduction) { this.bonReduction = bonReduction; }
}
