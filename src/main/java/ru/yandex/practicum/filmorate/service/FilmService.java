package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        getById(film.getId());
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
        Film film = getById(filmId);
        checkUserExists(userId);
        film.getLikes().add(userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getById(filmId);
        checkUserExists(userId);
        film.getLikes().remove(userId);
        log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше нуля");
        }
        return List.copyOf(filmStorage.findPopular(count));
    }

    private void checkUserExists(int userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
