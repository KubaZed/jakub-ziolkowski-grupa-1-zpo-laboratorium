package com.project.model;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.annotation.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Zadanie {

    @Id
    private Integer zadanieId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(max = 50, message = "Nazwa zadania może zawierać maksymalnie {max} znaków!")
    private String nazwa;

    private Integer kolejnosc;

    @Size(max = 1000, message = "Opis zadania może zawierać maksymalnie {max} znaków!")
    private String opis;

    @NotNull(message = "Pole dataczas dodania nie może być puste!")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataczasDodania;

    @JsonIgnoreProperties({"zadania"})
    private Projekt projekt;
}