package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", this::mapRowToMpa);
    }

    public Optional<Mpa> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?", this::mapRowToMpa, id)
                .stream()
                .findFirst();
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}
