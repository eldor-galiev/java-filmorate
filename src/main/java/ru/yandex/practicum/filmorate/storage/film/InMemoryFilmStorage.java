package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new LinkedHashMap<>();
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film add(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> findById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public void addLike(int filmId, int userId) {
        likes.computeIfAbsent(filmId, id -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        likes.getOrDefault(filmId, Set.of()).remove(userId);
    }

    @Override
    public Collection<Film> findPopular(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film film) -> likeCount(film.getId())).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private int likeCount(int filmId) {
        return likes.getOrDefault(filmId, Set.of()).size();
    }
}
