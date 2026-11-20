package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prestation")
public class Prestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestation")
    private Integer idPrestation;

    @Column(name = "libelle_prestation", length = 30, nullable = false, unique = true)
    private String libellePrestation;

    @Column(name = "duree_estimee")
    private Integer dureeEstimee;

    public Prestation() {}

    public Integer getIdPrestation() { return idPrestation; }
    public void setIdPrestation(Integer idPrestation) { this.idPrestation = idPrestation; }

    public String getLibellePrestation() { return libellePrestation; }
    public void setLibellePrestation(String libellePrestation) { this.libellePrestation = libellePrestation; }

    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
}
