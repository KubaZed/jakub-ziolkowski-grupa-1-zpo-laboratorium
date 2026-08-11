package com.project.model;

import java.util.Set;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {

    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    @Size(max = 50, message = "Imię może zawierać maksymalnie {max} znaków!")
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    @Size(max = 100, message = "Nazwisko może zawierać maksymalnie {max} znaków!")
    private String nazwisko;

    @NotBlank(message = "Pole nr indeksu nie może być puste!")
    @Size(max = 20, message = "Numer indeksu może zawierać maksymalnie {max} znaków!")
    private String nrIndeksu;

    @NotBlank(message = "Pole email nie może być puste!")
    @Email(message = "Niepoprawny format adresu email!")
    @Size(max = 50, message = "Email może zawierać maksymalnie {max} znaków!")
    private String email;

    @NotNull(message = "Pole stacjonarny nie może być puste!")
    private boolean stacjonarny;

    private Set<Projekt> projekty;
}