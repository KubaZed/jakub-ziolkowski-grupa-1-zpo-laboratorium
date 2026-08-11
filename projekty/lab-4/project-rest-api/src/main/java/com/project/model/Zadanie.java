package com.project.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Entity
@Table(name = "zadanie")
@Builder
public class Zadanie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zadanie_id")
    private Integer zadanieId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(max = 50, message = "Nazwa zadania może zawierać maksymalnie {max} znaków!")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column(name = "kolejnosc")
    private Integer kolejnosc;

    @Size(max = 1000, message = "Opis zadania może zawierać maksymalnie {max} znaków!")
    @Column(length = 1000)
    private String opis;

    @NotNull(message = "Pole dataczas dodania nie może być puste!")
    @Column(name = "dataczas_dodania", nullable = false)
    private LocalDateTime dataczasDodania;

    @ManyToOne
    @JoinColumn(name = "projekt_id")
    @JsonIgnoreProperties({"zadania"})
    private Projekt projekt;

    public Zadanie() {
    }

    public Zadanie(Integer zadanieId, String nazwa, Integer kolejnosc, String opis, LocalDateTime dataczasDodania, Projekt projekt) {
        this.zadanieId = zadanieId;
        this.nazwa = nazwa;
        this.kolejnosc = kolejnosc;
        this.opis = opis;
        this.dataczasDodania = dataczasDodania;
        this.projekt = projekt;
    }

    public Integer getZadanieId() {
        return zadanieId;
    }

    public void setZadanieId(Integer zadanieId) {
        this.zadanieId = zadanieId;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public Integer getKolejnosc() {
        return kolejnosc;
    }

    public void setKolejnosc(Integer kolejnosc) {
        this.kolejnosc = kolejnosc;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public LocalDateTime getDataczasDodania() {
        return dataczasDodania;
    }

    public void setDataczasDodania(LocalDateTime dataczasDodania) {
        this.dataczasDodania = dataczasDodania;
    }

    public Projekt getProjekt() {
        return projekt;
    }

    public void setProjekt(Projekt projekt) {
        this.projekt = projekt;
    }
}