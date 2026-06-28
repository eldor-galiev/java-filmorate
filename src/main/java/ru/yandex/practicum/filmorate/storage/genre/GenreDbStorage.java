package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id", this::mapRowToGenre);
    }

    public Optional<Genre> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM genres WHERE id = ?", this::mapRowToGenre, id)
                .stream()
                .findFirst();
    }

    public List<Genre> findByIds(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id IN (" + placeholders + ") ORDER BY id",
                this::mapRowToGenre,
                ids.toArray());
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}
