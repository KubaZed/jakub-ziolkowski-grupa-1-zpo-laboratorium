package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;
import com.validation.annotation.NrIndeksu;
import com.validation.annotation.ValidationFor;

@ValidationFor(NrIndeksu.class)
public class NrIndeksuStrategy implements ValidationStrategy {

    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(NrIndeksu.class) && value != null && value instanceof String) {
            NrIndeksu annotation = field.getAnnotation(NrIndeksu.class);
            String nrIndeksu = (String) value;

            if (!nrIndeksu.matches("\\d{8}")) {
                String errorInfo = String.format("Pole %s: %s", field.getName(), annotation.message());
                return Optional.of(errorInfo);
            }
        }
        return Optional.empty();
    }
}