package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemory")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private int nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(int id) {
        users.remove(id);
        friends.remove(id);
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void addFriend(int userId, int friendId) {
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        Set<Integer> userFriends = friends.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        Set<Integer> friendIds = friends.getOrDefault(userId, Set.of());
        return friendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriends = new HashSet<>(friends.getOrDefault(userId, Set.of()));
        userFriends.retainAll(friends.getOrDefault(otherId, Set.of()));
        return userFriends.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
