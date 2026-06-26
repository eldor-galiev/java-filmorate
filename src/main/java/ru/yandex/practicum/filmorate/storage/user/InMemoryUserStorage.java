package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private int nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
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
        friends.computeIfAbsent(userId, id -> new HashSet<>()).add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        friends.getOrDefault(userId, Set.of()).remove(friendId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        return friendIds(userId).stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> common = new HashSet<>(friendIds(userId));
        common.retainAll(friendIds(otherId));
        return common.stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    private Set<Integer> friendIds(int userId) {
        return friends.getOrDefault(userId, Set.of());
    }
}
