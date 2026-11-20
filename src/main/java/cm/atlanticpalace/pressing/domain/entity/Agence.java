package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "agence")
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agence")
    private Integer idAgence;

    @Column(name = "code_agence", length = 4, nullable = false, unique = true)
    private String codeAgence;

    @Column(name = "nom_agence", length = 30, nullable = false)
    private String nomAgence;

    @Column(name = "type_agence", length = 20, nullable = false)
    private String typeAgence;

    @Column(name = "adresse_agence", length = 120)
    private String adresseAgence;

    @Column(name = "tel_agence", length = 15)
    private String telAgence;

    public Agence() {}

    public Integer getIdAgence() { return idAgence; }
    public void setIdAgence(Integer idAgence) { this.idAgence = idAgence; }

    public String getCodeAgence() { return codeAgence; }
    public void setCodeAgence(String codeAgence) { this.codeAgence = codeAgence; }

    public String getNomAgence() { return nomAgence; }
    public void setNomAgence(String nomAgence) { this.nomAgence = nomAgence; }

    public String getTypeAgence() { return typeAgence; }
    public void setTypeAgence(String typeAgence) { this.typeAgence = typeAgence; }

    public String getAdresseAgence() { return adresseAgence; }
    public void setAdresseAgence(String adresseAgence) { this.adresseAgence = adresseAgence; }

    public String getTelAgence() { return telAgence; }
    public void setTelAgence(String telAgence) { this.telAgence = telAgence; }
}
