package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) {
        setNameIfBlank(user);
        return userStorage.add(user);
    }

    public User update(User user) {
        getById(user.getId());
        setNameIfBlank(user);
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public Collection<User> getAll() {
        return userStorage.findAll();
    }

    public void addFriend(int userId, int friendId) {
        checkDifferentUsers(userId, friendId);
        getById(userId);
        getById(friendId);
        userStorage.addFriend(userId, friendId);
        log.debug("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        checkDifferentUsers(userId, friendId);
        getById(userId);
        getById(friendId);
        userStorage.removeFriend(userId, friendId);
        log.debug("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        getById(userId);
        return List.copyOf(userStorage.getFriends(userId));
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        checkDifferentUsers(userId, otherId);
        getById(userId);
        getById(otherId);
        return List.copyOf(userStorage.getCommonFriends(userId, otherId));
    }

    private void setNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkDifferentUsers(int userId, int otherId) {
        if (userId == otherId) {
            throw new ValidationException("Идентификаторы пользователей должны быть разными");
        }
    }
}
