package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Primary
@Repository
public class FilmDbStorage implements FilmStorage {
    private static final String SELECT_FILM = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film add(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() == null ? null : film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> findById(int id) {
        List<Film> films = jdbcTemplate.query(SELECT_FILM + " WHERE f.id = ?", this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.getFirst();
        film.setGenres(findGenres(id));
        return Optional.of(film);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(SELECT_FILM + " ORDER BY f.id", this::mapRowToFilm);
        films.forEach(film -> film.setGenres(findGenres(film.getId())));
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public Collection<Film> findPopular(int count) {
        String sql = SELECT_FILM + """
                ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC, f.id
                LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        films.forEach(film -> film.setGenres(findGenres(film.getId())));
        return films;
    }

    private void saveGenres(Film film) {
        List<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .toList();
        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                genreIds,
                genreIds.size(),
                (ps, genreId) -> {
                    ps.setInt(1, film.getId());
                    ps.setInt(2, genreId);
                });
    }

    private List<Genre> findGenres(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.id "
                        + "WHERE fg.film_id = ? ORDER BY g.id",
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                filmId);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
        return film;
    }
}
