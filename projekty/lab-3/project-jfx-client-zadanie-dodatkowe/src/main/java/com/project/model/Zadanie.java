package com.project.model;

import java.time.LocalDateTime;

public class Zadanie {
    private Integer zadanieId;
    private Integer projektId;
    private String nazwa;
    private String opis;
    private Integer kolejnosc;
    private LocalDateTime dataczasUtworzenia;

    public Zadanie() {
        super();
    }

    public Zadanie(Integer zadanieId, Integer projektId, String nazwa, String opis, Integer kolejnosc, LocalDateTime dataczasUtworzenia) {
        super();
        this.zadanieId = zadanieId;
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.kolejnosc = kolejnosc;
        this.dataczasUtworzenia = dataczasUtworzenia;
    }

    public Integer getZadanieId() {
        return zadanieId;
    }

    public void setZadanieId(Integer zadanieId) {
        this.zadanieId = zadanieId;
    }

    public Integer getProjektId() {
        return projektId;
    }

    public void setProjektId(Integer projektId) {
        this.projektId = projektId;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public Integer getKolejnosc() {
        return kolejnosc;
    }

    public void setKolejnosc(Integer kolejnosc) {
        this.kolejnosc = kolejnosc;
    }

    public LocalDateTime getDataczasUtworzenia() {
        return dataczasUtworzenia;
    }

    public void setDataczasUtworzenia(LocalDateTime dataczasUtworzenia) {
        this.dataczasUtworzenia = dataczasUtworzenia;
    }
}