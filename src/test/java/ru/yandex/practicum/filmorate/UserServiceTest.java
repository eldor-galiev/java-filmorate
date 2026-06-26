package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    private User user(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private List<Integer> friendIds(int userId) {
        return userService.getFriends(userId).stream().map(User::getId).toList();
    }

    @Test
    void addUsesLoginWhenNameBlank() {
        User user = user("login");
        user.setName("  ");
        User created = userService.add(user);
        assertEquals("login", created.getName());
    }

    @Test
    void addFriendIsOneDirectional() {
        int firstId = userService.add(user("first")).getId();
        int secondId = userService.add(user("second")).getId();

        userService.addFriend(firstId, secondId);

        assertEquals(List.of(secondId), friendIds(firstId));
        assertTrue(friendIds(secondId).isEmpty());
    }

    @Test
    void removeFriendRemovesFriendship() {
        int firstId = userService.add(user("first")).getId();
        int secondId = userService.add(user("second")).getId();
        userService.addFriend(firstId, secondId);

        userService.removeFriend(firstId, secondId);

        assertTrue(friendIds(firstId).isEmpty());
    }

    @Test
    void getFriendsReturnsAddedFriend() {
        int firstId = userService.add(user("first")).getId();
        int secondId = userService.add(user("second")).getId();
        userService.addFriend(firstId, secondId);

        List<User> friends = userService.getFriends(firstId);

        assertEquals(1, friends.size());
        assertEquals(secondId, friends.getFirst().getId());
    }

    @Test
    void getCommonFriendsReturnsShared() {
        int firstId = userService.add(user("first")).getId();
        int secondId = userService.add(user("second")).getId();
        int commonId = userService.add(user("common")).getId();
        userService.addFriend(firstId, commonId);
        userService.addFriend(secondId, commonId);

        List<User> common = userService.getCommonFriends(firstId, secondId);

        assertEquals(1, common.size());
        assertEquals(commonId, common.getFirst().getId());
    }

    @Test
    void addFriendWithUnknownUserThrowsNotFound() {
        int firstId = userService.add(user("first")).getId();
        assertThrows(NotFoundException.class, () -> userService.addFriend(firstId, 999));
    }

    @Test
    void addFriendWithSameIdThrowsValidation() {
        int firstId = userService.add(user("first")).getId();
        assertThrows(ValidationException.class, () -> userService.addFriend(firstId, firstId));
    }

    @Test
    void removeFriendWithSameIdThrowsValidation() {
        int firstId = userService.add(user("first")).getId();
        assertThrows(ValidationException.class, () -> userService.removeFriend(firstId, firstId));
    }

    @Test
    void getCommonFriendsWithSameIdThrowsValidation() {
        int firstId = userService.add(user("first")).getId();
        assertThrows(ValidationException.class, () -> userService.getCommonFriends(firstId, firstId));
    }
}
