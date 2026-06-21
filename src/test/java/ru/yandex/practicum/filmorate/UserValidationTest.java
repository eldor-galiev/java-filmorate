package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Имя");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void validUserPassesValidation() {
        assertTrue(validator.validate(validUser()).isEmpty());
    }

    @Test
    void emptyEmailFails() {
        User user = validUser();
        user.setEmail("");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void emailWithoutAtSignFails() {
        User user = validUser();
        user.setEmail("userexample.com");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void validEmailPassesValidation() {
        User user = validUser();
        user.setEmail("correct@mail.ru");
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void emptyLoginFails() {
        User user = validUser();
        user.setLogin("");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void loginWithSpaceFails() {
        User user = validUser();
        user.setLogin("my login");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void loginWithoutSpacePassesValidation() {
        User user = validUser();
        user.setLogin("mylogin");
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void futureBirthdayFails() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void todayBirthdayPassesValidation() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void pastBirthdayPassesValidation() {
        User user = validUser();
        user.setBirthday(LocalDate.of(2000, 6, 15));
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void emptyNameIsAllowed() {
        User user = validUser();
        user.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }
}
