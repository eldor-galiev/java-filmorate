package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private UserStorage userStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = mock(UserStorage.class);
        userService = new UserService(userStorage);
    }

    private User user(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void addUsesLoginWhenNameBlank() {
        User user = user("login");
        user.setName("  ");
        when(userStorage.add(any())).thenAnswer(returnsFirstArg());

        User created = userService.add(user);

        assertEquals("login", created.getName());
    }

    @Test
    void addFriendWithUnknownUserThrowsNotFound() {
        when(userStorage.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(1, 2));
    }

    @Test
    void addFriendWithSameIdThrowsValidation() {
        assertThrows(ValidationException.class, () -> userService.addFriend(1, 1));
    }

    @Test
    void removeFriendWithSameIdThrowsValidation() {
        assertThrows(ValidationException.class, () -> userService.removeFriend(1, 1));
    }

    @Test
    void getCommonFriendsWithSameIdThrowsValidation() {
        assertThrows(ValidationException.class, () -> userService.getCommonFriends(1, 1));
    }
}
