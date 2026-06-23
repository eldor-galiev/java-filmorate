package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Genre> GENRE_ROW_MAPPER =
            (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"));

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", GENRE_ROW_MAPPER);
    }

    @Override
    public Optional<Genre> findById(int id) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id = ?", GENRE_ROW_MAPPER, id);
        return genres.stream().findFirst();
    }
}
