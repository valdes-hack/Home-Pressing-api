package cm.atlanticpalace.pressing.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité pour stocker les messages du formulaire de contact public.
 * Table : contact_message
 */
@Entity
@Table(name = "contact_message")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Integer idMessage;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "telephone", length = 15)
    private String telephone;

    @Column(name = "sujet", nullable = false, length = 200)
    private String sujet;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_reception", nullable = false)
    private LocalDateTime dateReception;

    @Column(name = "statut", nullable = false, length = 15)
    private String statut = "Non traité";

    @Column(name = "note_reponse", columnDefinition = "TEXT")
    private String noteReponse;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    // Constructeurs
    public ContactMessage() {
        this.dateReception = LocalDateTime.now();
        this.statut = "Non traité";
    }

    public ContactMessage(String nom, String email, String sujet, String message) {
        this();
        this.nom = nom;
        this.email = email;
        this.sujet = sujet;
        this.message = message;
    }

    // Getters et Setters
    public Integer getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(Integer idMessage) {
        this.idMessage = idMessage;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDateTime dateReception) {
        this.dateReception = dateReception;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNoteReponse() {
        return noteReponse;
    }

    public void setNoteReponse(String noteReponse) {
        this.noteReponse = noteReponse;
    }

    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(LocalDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    @Override
    public String toString() {
        return "ContactMessage{" +
                "idMessage=" + idMessage +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", sujet='" + sujet + '\'' +
                ", statut='" + statut + '\'' +
                ", dateReception=" + dateReception +
                '}';
    }
}
