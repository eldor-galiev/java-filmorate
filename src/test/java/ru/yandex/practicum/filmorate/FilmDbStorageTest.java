package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film createFilm(String name, int mpaId, Integer... genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        List<Genre> genres = new ArrayList<>();
        for (int genreId : genreIds) {
            Genre genre = new Genre();
            genre.setId(genreId);
            genres.add(genre);
        }
        film.setGenres(genres);
        return filmStorage.add(film);
    }

    private int createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.add(user).getId();
    }

    @Test
    void addPopulatesMpaNameAndGenres() {
        Film film = createFilm("Фильм", 1, 1, 2);

        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getMpa().getName()).isEqualTo("G");
                    assertThat(found.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
                    assertThat(found.getGenres()).extracting(Genre::getName)
                            .containsExactly("Комедия", "Драма");
                });
    }

    @Test
    void addDeduplicatesAndSortsGenres() {
        Film film = createFilm("Фильм", 3, 2, 1, 1);

        assertThat(filmStorage.findById(film.getId()))
                .hasValueSatisfying(found ->
                        assertThat(found.getGenres()).extracting(Genre::getId).containsExactly(1, 2));
    }

    @Test
    void findByIdUnknownReturnsEmpty() {
        assertThat(filmStorage.findById(9999)).isEmpty();
    }

    @Test
    void findAllReturnsCreatedFilms() {
        createFilm("Первый", 1);
        createFilm("Второй", 2);

        assertThat(filmStorage.findAll()).hasSize(2);
    }

    @Test
    void updateChangesFieldsAndGenres() {
        Film film = createFilm("Старое имя", 1, 1);
        film.setName("Новое имя");
        Mpa mpa = new Mpa();
        mpa.setId(2);
        film.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(3);
        film.setGenres(List.of(genre));

        filmStorage.update(film);

        assertThat(filmStorage.findById(film.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getName()).isEqualTo("Новое имя");
                    assertThat(found.getMpa().getId()).isEqualTo(2);
                    assertThat(found.getGenres()).extracting(Genre::getId).containsExactly(3);
                });
    }

    @Test
    void addLikeMovesFilmToTopOfPopular() {
        int withoutLike = createFilm("Без лайка", 1).getId();
        int liked = createFilm("С лайком", 1).getId();
        int userId = createUser("user");

        filmStorage.addLike(liked, userId);

        List<Integer> popularIds = filmStorage.findPopular(10).stream().map(Film::getId).toList();
        assertThat(popularIds).containsExactly(liked, withoutLike);
    }

    @Test
    void removeLikeReordersPopular() {
        int first = createFilm("Первый", 1).getId();
        int second = createFilm("Второй", 1).getId();
        int userId = createUser("user");
        filmStorage.addLike(first, userId);

        filmStorage.removeLike(first, userId);
        filmStorage.addLike(second, userId);

        assertThat(filmStorage.findPopular(1)).extracting(Film::getId).containsExactly(second);
    }
}
