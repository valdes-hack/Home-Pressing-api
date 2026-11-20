package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "produit")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Integer idProduit;

    @Column(name = "libelle_produit", length = 40, nullable = false, unique = true)
    private String libelleProduit;

    @Column(name = "categorie", length = 25)
    private String categorie;

    @Column(name = "unite_mesure", length = 12)
    private String uniteMesure;

    @Column(name = "seuil_alerte", nullable = false)
    private Integer seuilAlerte = 0;

    public Produit() {}

    public Integer getIdProduit() { return idProduit; }
    public void setIdProduit(Integer idProduit) { this.idProduit = idProduit; }

    public String getLibelleProduit() { return libelleProduit; }
    public void setLibelleProduit(String libelleProduit) { this.libelleProduit = libelleProduit; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getUniteMesure() { return uniteMesure; }
    public void setUniteMesure(String uniteMesure) { this.uniteMesure = uniteMesure; }

    public Integer getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(Integer seuilAlerte) { this.seuilAlerte = seuilAlerte; }
}
