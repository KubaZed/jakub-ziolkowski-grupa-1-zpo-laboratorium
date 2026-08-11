package com.validation.strategy;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.validation.annotation.ValidationFor;

public class ValidationStrategyFactory {

	private static final Map<Class<? extends Annotation>, ValidationStrategy> 
		strategies = new HashMap<>();

	static {
		registerStrategies();
	}
	
	private ValidationStrategyFactory() {
	}
	
	private static void registerStrategies() {
        Reflections reflections = new Reflections("com.validation.strategy");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(ValidationFor.class);

        for (Class<?> clazz : classes) {
            if (ValidationStrategy.class.isAssignableFrom(clazz)) {
                ValidationFor annotation = clazz.getAnnotation(ValidationFor.class);

                try {
                    ValidationStrategy strategy =
                            (ValidationStrategy) clazz.getDeclaredConstructor().newInstance();
                    strategies.put(annotation.value(), strategy);
                } catch (Exception e) {
                    throw new RuntimeException("Błąd rejestracji strategii: " + clazz.getName(), e);
                }
            }
        }
    }
	
	public static ValidationStrategy getStrategy(Annotation annotation) {
		return strategies.get(annotation.annotationType());
	}
}