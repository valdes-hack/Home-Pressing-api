package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Integer idClient;

    @Column(name = "nom_client", length = 50, nullable = false)
    private String nomClient;

    @Column(name = "tel_client", length = 15, nullable = false, unique = true)
    private String telClient;

    @Column(name = "points_fidelite", nullable = false)
    private Integer pointsFidelite = 0;

    public Client() {}

    public Integer getIdClient() { return idClient; }
    public void setIdClient(Integer idClient) { this.idClient = idClient; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getTelClient() { return telClient; }
    public void setTelClient(String telClient) { this.telClient = telClient; }

    public Integer getPointsFidelite() { return pointsFidelite; }
    public void setPointsFidelite(Integer pointsFidelite) { this.pointsFidelite = pointsFidelite; }
}
