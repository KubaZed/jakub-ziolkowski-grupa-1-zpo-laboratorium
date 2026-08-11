package com.project.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student", indexes = {
    @Index(name = "idx_nazwisko", columnList = "nazwisko", unique = false),
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_nr_indeksu", columnList = "nr_indeksu", unique = true)
})
public class Student implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    @Size(max = 50, message = "Imię może zawierać maksymalnie {max} znaków!")
    @Column(nullable = false, length = 50)
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    @Size(max = 100, message = "Nazwisko może zawierać maksymalnie {max} znaków!")
    @Column(nullable = false, length = 100)
    private String nazwisko;

    @NotBlank(message = "Pole nr indeksu nie może być puste!")
    @Size(max = 20, message = "Numer indeksu może zawierać maksymalnie {max} znaków!")
    @Column(name = "nr_indeksu", nullable = false, unique = true, length = 20)
    private String nrIndeksu;

    @NotEmpty(message = "Nie podano adresu e-mail")
    @Email(message = "Niepoprawny format adresu e-mail")
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @JsonProperty(access = Access.WRITE_ONLY)
    @Size(min = 8, max = 64, message = "Hasło musi składać się z przynajmniej {min} i nie przekraczać {max} znaków")
    private String password;

    @NotNull(message = "Pole stacjonarny nie może być puste!")
    @Column(nullable = false)
    private boolean stacjonarny;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "student_role",
               joinColumns = {@JoinColumn(name = "student_id")},
               inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private Set<Role> roles;

    @ManyToMany(mappedBy = "studenci")
    @JsonIgnoreProperties({"studenci"})
    private Set<Projekt> projekty;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.email; //zakładamy, że e-mail będzie wykorzystywany przy logowaniu
    }
}