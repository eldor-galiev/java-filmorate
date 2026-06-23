package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }
        return user;
    };

    @Override
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findById(int id) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users WHERE id = ?",
                USER_ROW_MAPPER, id);
        return users.stream().findFirst();
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users ORDER BY id",
                USER_ROW_MAPPER);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "MERGE INTO friends KEY(user_id, friend_id) VALUES (?, ?)",
                userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friends WHERE user_id = ? AND friend_id = ?",
                userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM users u JOIN friends f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? ORDER BY u.id",
                USER_ROW_MAPPER, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM users u " +
                "JOIN friends f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friends f2 ON u.id = f2.friend_id AND f2.user_id = ? " +
                "ORDER BY u.id",
                USER_ROW_MAPPER, userId, otherId);
    }
}
