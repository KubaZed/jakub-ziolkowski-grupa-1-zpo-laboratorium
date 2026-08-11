package com.validation;

import com.validation.exception.ValidationException;
import com.validation.validator.Validator;

public class Main {
	public static void main(String[] args) {
		try {
			Student student = new Student();
			student.setImie("Al");
	        student.setNazwisko("");
	        student.setNrIndeksu("1234");
			student.setEmail("Grzegorz.Brzęczyszczykiewicz#pbs.edu.pl");
			
			Validator.validate(student);
		} catch (ValidationException e) {
			System.out.println(e.getMessage());
		}
	}
}