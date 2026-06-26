package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    Optional<Film> findById(int id);

    Collection<Film> findAll();

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    Collection<Film> findPopular(int count);
}
