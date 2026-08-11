package com.project.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record Credentials(
		@NotBlank(message = "Nie podano adresu e-mail")
		@Email(message = "Niepoprawny format adresu e-mail")
		String email,

		@NotNull(message = "Nie podano hasła")
		@Size(min = 8, max = 25,
					   message = "Hasło musi składać się z przynajmniej {min} znaków i nie przekraczać {max}")
		String password
) {}