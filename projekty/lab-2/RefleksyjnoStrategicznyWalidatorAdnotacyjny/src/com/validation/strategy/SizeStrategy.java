package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;
import com.validation.annotation.Size;

public class SizeStrategy implements ValidationStrategy {

    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(Size.class) && value != null && value instanceof String) {
            Size annotation = field.getAnnotation(Size.class);
            int length = ((String) value).length();

            if (length < annotation.min() || length > annotation.max()) {
                String errorInfo = String.format("Pole %s: %s", field.getName(), annotation.message());
                return Optional.of(errorInfo);
            }
        }
        return Optional.empty();
    }
}