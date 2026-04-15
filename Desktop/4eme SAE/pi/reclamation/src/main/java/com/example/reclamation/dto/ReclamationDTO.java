package com.example.reclamation.dto;

import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;

import java.util.Date;

public class ReclamationDTO {

    private Integer idReclamation;
    private String sujet;
    private String description;
    private Date dateCreation;
    private Statut statut;
    private Priorite priorite;
    private Type type;

    // 🔥 nouveaux champs
    private boolean urgent;
    private String urgentReason;

    // getters & setters

    public Integer getIdReclamation() {
        return idReclamation;
    }

    public void setIdReclamation(Integer idReclamation) {
        this.idReclamation = idReclamation;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Priorite getPriorite() {
        return priorite;
    }

    public void setPriorite(Priorite priorite) {
        this.priorite = priorite;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public String getUrgentReason() {
        return urgentReason;
    }

    public void setUrgentReason(String urgentReason) {
        this.urgentReason = urgentReason;
    }
}