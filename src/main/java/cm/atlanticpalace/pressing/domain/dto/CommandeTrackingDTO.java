package cm.atlanticpalace.pressing.domain.dto;

import java.time.LocalDate;

/**
 * DTO public pour le suivi de commande (endpoint tracking).
 * Contient uniquement les informations non sensibles affichables au client.
 * Utilisé par l'application home-pressing (site client).
 */
public class CommandeTrackingDTO {
    
    private String numFacture;
    private String statutCmd;
    private LocalDate dateDepot;
    private String nomAgence;
    private String codeAgence;
    private Integer nbArticles;
    private Integer progression; // 0-100%
    private String message;

    // Constructeurs
    public CommandeTrackingDTO() {}

    public CommandeTrackingDTO(String numFacture, String statutCmd, LocalDate dateDepot, 
                               String nomAgence, String codeAgence, Integer nbArticles, 
                               Integer progression, String message) {
        this.numFacture = numFacture;
        this.statutCmd = statutCmd;
        this.dateDepot = dateDepot;
        this.nomAgence = nomAgence;
        this.codeAgence = codeAgence;
        this.nbArticles = nbArticles;
        this.progression = progression;
        this.message = message;
    }

    // Getters et Setters
    public String getNumFacture() {
        return numFacture;
    }

    public void setNumFacture(String numFacture) {
        this.numFacture = numFacture;
    }

    public String getStatutCmd() {
        return statutCmd;
    }

    public void setStatutCmd(String statutCmd) {
        this.statutCmd = statutCmd;
    }

    public LocalDate getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDate dateDepot) {
        this.dateDepot = dateDepot;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public void setNomAgence(String nomAgence) {
        this.nomAgence = nomAgence;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public Integer getNbArticles() {
        return nbArticles;
    }

    public void setNbArticles(Integer nbArticles) {
        this.nbArticles = nbArticles;
    }

    public Integer getProgression() {
        return progression;
    }

    public void setProgression(Integer progression) {
        this.progression = progression;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CommandeTrackingDTO{" +
                "numFacture='" + numFacture + '\'' +
                ", statutCmd='" + statutCmd + '\'' +
                ", dateDepot=" + dateDepot +
                ", nomAgence='" + nomAgence + '\'' +
                ", codeAgence='" + codeAgence + '\'' +
                ", nbArticles=" + nbArticles +
                ", progression=" + progression +
                ", message='" + message + '\'' +
                '}';
    }
}
