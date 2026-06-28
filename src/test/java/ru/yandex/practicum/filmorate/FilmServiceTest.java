package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilmServiceTest {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private MpaDbStorage mpaStorage;
    private GenreDbStorage genreStorage;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmStorage = mock(FilmStorage.class);
        userStorage = mock(UserStorage.class);
        mpaStorage = mock(MpaDbStorage.class);
        genreStorage = mock(GenreDbStorage.class);
        filmService = new FilmService(filmStorage, userStorage, mpaStorage, genreStorage);
    }

    private Film film() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        return film;
    }

    @Test
    void addWithUnknownMpaThrowsNotFound() {
        when(mpaStorage.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.add(film()));
    }

    @Test
    void addWithUnknownGenreThrowsNotFound() {
        Film film = film();
        Genre genre = new Genre();
        genre.setId(99);
        film.setGenres(List.of(genre));
        when(mpaStorage.findById(anyInt())).thenReturn(Optional.of(new Mpa()));
        when(genreStorage.findByIds(List.of(99))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> filmService.add(film));
    }

    @Test
    void addLikeToUnknownFilmThrowsNotFound() {
        when(filmStorage.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(999, 1));
    }

    @Test
    void addLikeFromUnknownUserThrowsNotFound() {
        when(filmStorage.findById(anyInt())).thenReturn(Optional.of(film()));
        when(userStorage.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1, 999));
    }

    @Test
    void getPopularWithZeroCountThrowsValidation() {
        assertThrows(ValidationException.class, () -> filmService.getPopular(0));
    }

    @Test
    void getPopularWithNegativeCountThrowsValidation() {
        assertThrows(ValidationException.class, () -> filmService.getPopular(-1));
    }
}
