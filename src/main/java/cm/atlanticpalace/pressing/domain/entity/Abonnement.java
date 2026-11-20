package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "abonnement")
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abonnement")
    private Integer idAbonnement;

    @Column(name = "libelle_abonnement", length = 40, nullable = false)
    private String libelleAbonnement;

    @Column(name = "cout_mensuel", precision = 10, scale = 2, nullable = false)
    private BigDecimal coutMensuel;

    @Column(name = "nb_lavages_inclus", nullable = false)
    private Integer nbLavagesInclus;

    @Column(name = "nb_lavages_restants", nullable = false)
    private Integer nbLavagesRestants;

    @Column(name = "date_debut_abt", nullable = false)
    private LocalDate dateDebutAbt;

    @Column(name = "date_fin_abt", nullable = false)
    private LocalDate dateFinAbt;

    @Column(name = "statut_abonnement", length = 12, nullable = false)
    private String statutAbonnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    public Abonnement() {}

    public Integer getIdAbonnement() { return idAbonnement; }
    public void setIdAbonnement(Integer idAbonnement) { this.idAbonnement = idAbonnement; }

    public String getLibelle_abonnement() { return libelleAbonnement; }
    public void setLibelleAbonnement(String libelleAbonnement) { this.libelleAbonnement = libelleAbonnement; }

    public BigDecimal getCoutMensuel() { return coutMensuel; }
    public void setCoutMensuel(BigDecimal coutMensuel) { this.coutMensuel = coutMensuel; }

    public Integer getNbLavagesInclus() { return nbLavagesInclus; }
    public void setNbLavagesInclus(Integer nbLavagesInclus) { this.nbLavagesInclus = nbLavagesInclus; }

    public Integer getNbLavagesRestants() { return nbLavagesRestants; }
    public void setNbLavagesRestants(Integer nbLavagesRestants) { this.nbLavagesRestants = nbLavagesRestants; }

    public LocalDate getDateDebutAbt() { return dateDebutAbt; }
    public void setDateDebutAbt(LocalDate dateDebutAbt) { this.dateDebutAbt = dateDebutAbt; }

    public LocalDate getDateFinAbt() { return dateFinAbt; }
    public void setDateFinAbt(LocalDate dateFinAbt) { this.dateFinAbt = dateFinAbt; }

    public String getStatutAbonnement() { return statutAbonnement; }
    public void setStatutAbonnement(String statutAbonnement) { this.statutAbonnement = statutAbonnement; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
