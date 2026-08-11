package com.project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "projekt", indexes = @Index(name = "idx_nazwa_projektu", columnList = "nazwa"))
@Builder
public class Projekt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projekt_id")
    private Integer projektId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(min = 3, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków!")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column(length = 1000)
    private String opis;

    @CreatedDate
    @Column(name = "dataczas_utworzenia", nullable = false, updatable = false)
    private LocalDateTime dataczasUtworzenia;

    @LastModifiedDate
    @Column(name = "dataczas_modyfikacji", insertable = false)
    private LocalDateTime dataczasModyfikacji;

    @Column(name = "data_oddania")
    private LocalDate dataOddania;

    @OneToMany(mappedBy = "projekt")
    @JsonIgnoreProperties({"projekt"})
    private List<Zadanie> zadania;

    @ManyToMany
    @JoinTable(name = "projekt_student",
               joinColumns = {@JoinColumn(name = "projekt_id")},
               inverseJoinColumns = {@JoinColumn(name = "student_id")})
    @JsonIgnoreProperties({"projekty"})
    private Set<Student> studenci;

    public Projekt() {
    }

    public Projekt(Integer projektId, String nazwa, String opis, LocalDateTime dataczasUtworzenia, LocalDateTime dataczasModyfikacji, LocalDate dataOddania) {
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.dataczasUtworzenia = dataczasUtworzenia;
        this.dataczasModyfikacji = dataczasModyfikacji;
        this.dataOddania = dataOddania;
    }

    public Projekt(Integer projektId, String nazwa, String opis, LocalDateTime dataczasUtworzenia, LocalDateTime dataczasModyfikacji, LocalDate dataOddania, List<Zadanie> zadania) {
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.dataczasUtworzenia = dataczasUtworzenia;
        this.dataczasModyfikacji = dataczasModyfikacji;
        this.dataOddania = dataOddania;
        this.zadania = zadania;
    }

    public Projekt(Integer projektId, String nazwa, String opis, LocalDateTime dataczasUtworzenia, LocalDateTime dataczasModyfikacji, LocalDate dataOddania, List<Zadanie> zadania, Set<Student> studenci) {
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.dataczasUtworzenia = dataczasUtworzenia;
        this.dataczasModyfikacji = dataczasModyfikacji;
        this.dataOddania = dataOddania;
        this.zadania = zadania;
        this.studenci = studenci;
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

    public LocalDateTime getDataczasUtworzenia() {
        return dataczasUtworzenia;
    }

    public void setDataczasUtworzenia(LocalDateTime dataczasUtworzenia) {
        this.dataczasUtworzenia = dataczasUtworzenia;
    }

    public LocalDateTime getDataczasModyfikacji() {
        return dataczasModyfikacji;
    }

    public void setDataczasModyfikacji(LocalDateTime dataczasModyfikacji) {
        this.dataczasModyfikacji = dataczasModyfikacji;
    }

    public LocalDate getDataOddania() {
        return dataOddania;
    }

    public void setDataOddania(LocalDate dataOddania) {
        this.dataOddania = dataOddania;
    }

    public List<Zadanie> getZadania() {
        return zadania;
    }

    public void setZadania(List<Zadanie> zadania) {
        this.zadania = zadania;
    }

    public Set<Student> getStudenci() {
        return studenci;
    }

    public void setStudenci(Set<Student> studenci) {
        this.studenci = studenci;
    }
}