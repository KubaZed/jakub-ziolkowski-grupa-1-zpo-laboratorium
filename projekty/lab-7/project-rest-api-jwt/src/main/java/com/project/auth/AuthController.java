package com.project.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.model.Student;
import com.project.validation.ValidationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;
    private final ValidationService<Student> studentValidator;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody Student student) {
        studentValidator.validate(student);
        authService.register(student);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    //Sprawdź zawartość zwracanego tokenu np. na https://jwt.io/
    @PostMapping("/login")
    public ResponseEntity<Tokens> login(@RequestBody Credentials credentials) {
        return ResponseEntity.ok(authService.authenticate(credentials));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Tokens> refreshToken(@RequestBody Tokens tokens) {
        return ResponseEntity.ok(authService.refreshTokens(tokens));
    }
}