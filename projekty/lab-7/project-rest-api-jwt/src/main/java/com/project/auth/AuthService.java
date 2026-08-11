package com.project.auth;

import com.project.model.Student;

public interface AuthService {
	void register(Student student);
	Tokens authenticate(Credentials credentials);
	Tokens refreshTokens(Tokens tokens); 
}