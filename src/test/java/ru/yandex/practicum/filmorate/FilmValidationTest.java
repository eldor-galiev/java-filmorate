package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void validFilmPassesValidation() {
        assertTrue(validator.validate(validFilm()).isEmpty());
    }

    @Test
    void blankNameFails() {
        Film film = validFilm();
        film.setName("  ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void emptyNameFails() {
        Film film = validFilm();
        film.setName("");
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void descriptionExactly200CharsPassesValidation() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void description201CharsFails() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void releaseDateExactlyMinDatePassesValidation() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void releaseDateBeforeMinDateFails() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void negativeDurationFails() {
        Film film = validFilm();
        film.setDuration(-1);
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void zeroDurationFails() {
        Film film = validFilm();
        film.setDuration(0);
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void positiveDurationPassesValidation() {
        Film film = validFilm();
        film.setDuration(1);
        assertTrue(validator.validate(film).isEmpty());
    }
}
