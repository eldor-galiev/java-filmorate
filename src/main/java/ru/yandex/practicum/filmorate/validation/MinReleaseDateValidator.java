package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MinReleaseDateValidator implements ConstraintValidator<MinReleaseDate, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(MinReleaseDate annotation) {
        minDate = LocalDate.parse(annotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isBefore(minDate);
    }
}
