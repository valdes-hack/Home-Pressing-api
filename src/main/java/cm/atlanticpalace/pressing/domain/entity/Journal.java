package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "journal")
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_journal")
    private Integer idJournal;

    @Column(name = "date_heure", nullable = false, updatable = false)
    private LocalDateTime dateHeure;

    @Column(name = "action_journal", length = 100, nullable = false, updatable = false)
    private String actionJournal;

    @Column(name = "cible_type", length = 30, updatable = false)
    private String cibleType;

    @Column(name = "cible_id", length = 14, updatable = false)
    private String cibleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false, updatable = false)
    private Utilisateur utilisateur;

    public Journal() {}

    public Integer getIdJournal() { return idJournal; }
    public void setIdJournal(Integer idJournal) { this.idJournal = idJournal; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getActionJournal() { return actionJournal; }
    public void setActionJournal(String actionJournal) { this.actionJournal = actionJournal; }

    public String getCibleType() { return cibleType; }
    public void setCibleType(String cibleType) { this.cibleType = cibleType; }

    public String getCibleId() { return cibleId; }
    public void setCibleId(String cibleId) { this.cibleId = cibleId; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
