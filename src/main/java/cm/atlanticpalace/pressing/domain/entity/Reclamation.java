package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamation")
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclamation")
    private Integer idReclamation;

    @Column(name = "motif", length = 50, nullable = false)
    private String motif;

    @Column(name = "description_recl", length = 255)
    private String descriptionRecl;

    @Column(name = "statut_reclamation", length = 15, nullable = false)
    private String statutReclamation;

    @Column(name = "date_reclamation", nullable = false)
    private LocalDateTime dateReclamation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "num_facture", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    public Reclamation() {}

    public Integer getIdReclamation() { return idReclamation; }
    public void setIdReclamation(Integer idReclamation) { this.idReclamation = idReclamation; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getDescriptionRecl() { return descriptionRecl; }
    public void setDescriptionRecl(String descriptionRecl) { this.descriptionRecl = descriptionRecl; }

    public String getStatutReclamation() { return statutReclamation; }
    public void setStatutReclamation(String statutReclamation) { this.statutReclamation = statutReclamation; }

    public LocalDateTime getDateReclamation() { return dateReclamation; }
    public void setDateReclamation(LocalDateTime dateReclamation) { this.dateReclamation = dateReclamation; }

    public LocalDateTime getDateResolution() { return dateResolution; }
    public void setDateResolution(LocalDateTime dateResolution) { this.dateResolution = dateResolution; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
