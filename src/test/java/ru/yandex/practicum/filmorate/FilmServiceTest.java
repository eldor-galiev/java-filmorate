package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private int userId;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage,
                Mockito.mock(MpaDbStorage.class), Mockito.mock(GenreDbStorage.class));
        userId = userStorage.add(user()).getId();
    }

    private Film film(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    private User user() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setName("Имя");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void addLikeMakesFilmPopular() {
        filmService.add(film("Без лайка"));
        int liked = filmService.add(film("С лайком")).getId();

        filmService.addLike(liked, userId);

        assertEquals(liked, filmService.getPopular(10).getFirst().getId());
    }

    @Test
    void addLikeToUnknownFilmThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> filmService.addLike(999, userId));
    }

    @Test
    void addLikeFromUnknownUserThrowsNotFound() {
        int filmId = filmService.add(film("Фильм")).getId();
        assertThrows(NotFoundException.class, () -> filmService.addLike(filmId, 999));
    }

    @Test
    void removeLikeDropsPopularity() {
        int first = filmService.add(film("Первый")).getId();
        int second = filmService.add(film("Второй")).getId();
        filmService.addLike(first, userId);
        assertEquals(first, filmService.getPopular(10).getFirst().getId());

        filmService.removeLike(first, userId);
        filmService.addLike(second, userId);

        assertEquals(second, filmService.getPopular(10).getFirst().getId());
    }

    @Test
    void getPopularSortsByLikes() {
        int filmWithoutLikes = filmService.add(film("Без лайков")).getId();
        int filmWithLike = filmService.add(film("С лайком")).getId();
        filmService.addLike(filmWithLike, userId);

        List<Film> popular = filmService.getPopular(10);

        assertEquals(2, popular.size());
        assertEquals(filmWithLike, popular.get(0).getId());
        assertEquals(filmWithoutLikes, popular.get(1).getId());
    }

    @Test
    void getPopularRespectsCount() {
        filmService.add(film("Первый"));
        filmService.add(film("Второй"));
        filmService.add(film("Третий"));
        assertEquals(2, filmService.getPopular(2).size());
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
