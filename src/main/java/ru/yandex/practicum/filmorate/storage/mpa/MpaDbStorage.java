package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Mpa> MPA_ROW_MAPPER =
            (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name"));

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings ORDER BY id", MPA_ROW_MAPPER);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT id, name FROM mpa_ratings WHERE id = ?", MPA_ROW_MAPPER, id);
        return ratings.stream().findFirst();
    }
}
