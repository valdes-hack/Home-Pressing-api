package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "historique_statut")
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer idHistorique;

    @Column(name = "ancien_statut", length = 20, nullable = false, updatable = false)
    private String ancienStatut;

    @Column(name = "nouveau_statut", length = 20, nullable = false, updatable = false)
    private String nouveauStatut;

    @Column(name = "date_heure_statut", nullable = false, updatable = false)
    private LocalDateTime dateHeureStatut;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_article", nullable = false, updatable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false, updatable = false)
    private Utilisateur utilisateur;

    public HistoriqueStatut() {}

    public Integer getIdHistorique() { return idHistorique; }
    public void setIdHistorique(Integer idHistorique) { this.idHistorique = idHistorique; }

    public String getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(String ancienStatut) { this.ancienStatut = ancienStatut; }

    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public LocalDateTime getDateHeureStatut() { return dateHeureStatut; }
    public void setDateHeureStatut(LocalDateTime dateHeureStatut) { this.dateHeureStatut = dateHeureStatut; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
