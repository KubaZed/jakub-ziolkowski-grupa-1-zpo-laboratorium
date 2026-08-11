package com.project.validation;

import org.springframework.stereotype.Service;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService<T> {
	private final Validator validator;
	public void validate(T object) {
		var violations = validator.validate(object);
		if (!violations.isEmpty())
			throw new ConstraintViolationException(violations);
	} 
}