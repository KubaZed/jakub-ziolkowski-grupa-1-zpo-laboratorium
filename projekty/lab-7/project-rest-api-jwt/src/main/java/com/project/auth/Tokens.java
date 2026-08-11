package com.project.auth;

import lombok.Builder;

@Builder
public record Tokens(
		String accessToken,
		String refreshToken 
) {}