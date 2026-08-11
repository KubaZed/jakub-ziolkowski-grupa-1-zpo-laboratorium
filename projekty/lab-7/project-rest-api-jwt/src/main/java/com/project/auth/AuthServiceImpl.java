package com.project.auth;

import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.project.config.JwtService;
import com.project.model.Student;
import com.project.repository.RoleRepository;
import com.project.service.StudentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final StudentService studentService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void register(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        var role = roleRepository
                .findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER was not initiated"));
        student.setRoles(Set.of(role));
        studentService.setStudent(student);
    }

    @Override
    public Tokens authenticate(Credentials credentials) {
        return authenticate(credentials.email(), credentials.password());
    }

    private Tokens authenticate(@NonNull String email, @NonNull String password) {
        // Uwierzytelnianie użytkownika na podstawie podanego adresu e-mail i hasła,
        // poprzez delegację weryfikacji do komponentu AuthenticationManager
        // skonfigurowanego w kontekście Spring Security.
        // Poniższa metoda sprawdza, czy poświadczenia są poprawne. Jeśli login lub hasło
        // są błędne, metoda ta rzuci wyjątek (AuthenticationException)
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));

        // Pobranie obiektu użytkownika (już po potwierdzeniu tożsamości), aby móc wygenerować
        // dla niego tokeny
        var user = studentService
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User '%s' not found!", email)));
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return Tokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public Tokens refreshTokens(Tokens tokens) {
        final String refreshToken = tokens.refreshToken();
        if (refreshToken == null || refreshToken.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak tokenu odświeżania");
        final String prefix = "Bearer ";
        final String token = refreshToken.startsWith(prefix) ?
                refreshToken.substring(prefix.length()) : refreshToken;
        final String email = jwtService.extractUserNameFromRefreshToken(token);
        if (email == null || email.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Niepoprawny format tokenu odświeżania");
        var user = studentService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User '%s' not found!", email)));
        if (!jwtService.isRefreshTokenValid(token, user))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token odświeżania stracił ważność");
        var accessToken = jwtService.generateAccessToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);
        return Tokens.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}