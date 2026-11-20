package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer idNotification;

    @Column(name = "date_envoi_sms")
    private LocalDateTime dateEnvoiSms;

    @Column(name = "statut_sms", length = 10, nullable = false)
    private String statutSms;

    @Column(name = "nb_renvois", nullable = false)
    private Integer nbRenvois = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "num_facture", nullable = false)
    private Commande commande;

    public Notification() {}

    public Integer getIdNotification() { return idNotification; }
    public void setIdNotification(Integer idNotification) { this.idNotification = idNotification; }

    public LocalDateTime getDateEnvoiSms() { return dateEnvoiSms; }
    public void setDateEnvoiSms(LocalDateTime dateEnvoiSms) { this.dateEnvoiSms = dateEnvoiSms; }

    public String getStatutSms() { return statutSms; }
    public void setStatutSms(String statutSms) { this.statutSms = statutSms; }

    public Integer getNbRenvois() { return nbRenvois; }
    public void setNbRenvois(Integer nbRenvois) { this.nbRenvois = nbRenvois; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }
}
