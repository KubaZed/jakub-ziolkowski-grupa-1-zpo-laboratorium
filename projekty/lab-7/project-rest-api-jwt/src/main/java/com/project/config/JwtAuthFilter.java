package com.project.config;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    // automatycznie wstrzykiwany (zdefiniowany jako @Bean w SecurityConfig)
    private final UserDetailsService userDetailsService;

    // gdy przychodzi żądanie HTTP to filtr zdefiniowany w metodzie doFilterInternal(...)
    // je przechwytuje i sprawdza, czy zawiera w nagłówku Authorization poprawny token JWT
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); //np. Bearer eyJhb...
        final String prefix = "Bearer ";
        if (authHeader == null || !authHeader.startsWith(prefix)) {
            // brak tokenu w nagłówku, tylko przetwarzanie przez kolejne filtry w łańcuchu
            filterChain.doFilter(request, response);
            // i przerwanie realizacji metody
            return;
        }
        final String token = authHeader.substring(prefix.length());
        try {
            final String userName = jwtService.extractUserNameFromAccessToken(token);
            if (userName != null
                    //Druga cześć warunku sprawdza czy użytkownik po raz pierwszy jest uwierzytelniany.
                    //Zapobiega to ponownemu, niepotrzebnemu procesowi uwierzytelniania, jeśli jakiś
                    //inny filtr wcześniej w tym samym łańcuchu (request chain) już to zrobił.
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);
                if (jwtService.isAccessTokenValid(token, userDetails)) {
                    // Tworzenie obiektu Authentication (authToken). Klasa UsernamePasswordAuthenticationToken
                    // ze Spring Security jest jedną z implementacji interfejsu Authentication i służy do
                    // w reprezentowania procesu uwierzytelnienia użytkownika na podstawie nazwy użytkownika
                    // i hasła. Pierwszy parametr konstruktora reprezentuje tożsamość użytkownika, np. login
                    // lub obiekt użytkownika załadowany z bazy danych. Drugi zawiera dane uwierzytelnienia,
                    // najczęściej jest to hasło użytkownika. Po uwierzytelnieniu zazwyczaj dla bezpieczeństwa
                    // wartość tego pola zostaje wyczyszczona. W naszym przypadku od razu przekazujemy null.
                    // Ostatni parametr to kolekcja ról (uprawnień) przypisanych do użytkownika.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Obiekt Authentication (authToken) jest zapisywany w SecurityContext, który jest
                    // zarządzany przez SecurityContextHolder. Dzięki temu inne komponenty aplikacji
                    // mogą łatwo uzyskać dostęp do danych uwierzytelnienia.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    // SecurityContextHolder udostępnia dane o uwierzytelnionym użytkowniku i
                    // w obrębie jednego wątku zawsze zwróci ten sam SecurityContext. Domyślnie
                    // używa obiektu ThreadLocal do przechowywania kontekstu bezpieczeństwa,
                    // co oznacza, że kontekst bezpieczeństwa jest zawsze dostępny dla metod w
                    // tym samym wątku wykonania.
                }
            }
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        // o tym trzeba pamiętać, aby umożliwić przetwarzanie przez kolejne filtry w łańcuchu
        filterChain.doFilter(request, response);
    }
}