package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       MpaDbStorage mpaStorage,
                       GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film add(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        getById(film.getId());
        validateMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public Collection<Film> getAll() {
        return filmStorage.findAll();
    }

    public void addLike(int filmId, int userId) {
        getById(filmId);
        checkUserExists(userId);
        filmStorage.addLike(filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        getById(filmId);
        checkUserExists(userId);
        filmStorage.removeLike(filmId, userId);
        log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше нуля");
        }
        return List.copyOf(filmStorage.findPopular(count));
    }

    private void validateMpaAndGenres(Film film) {
        mpaStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + film.getMpa().getId() + " не найден"));

        List<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .toList();
        if (genreIds.isEmpty()) {
            return;
        }
        List<Integer> foundIds = genreStorage.findByIds(genreIds).stream()
                .map(Genre::getId)
                .toList();
        if (foundIds.size() < genreIds.size()) {
            int missingId = genreIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new NotFoundException("Жанр с id=" + missingId + " не найден");
        }
    }

    private void checkUserExists(int userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
