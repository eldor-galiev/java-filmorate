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
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film makeFilm(String name, int mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(mpaId, null));
        return film;
    }

    @Test
    void testFindUserById() {
        User user = userStorage.add(makeUser("test@mail.ru", "testuser"));
        Optional<User> found = userStorage.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testFindAllUsers() {
        userStorage.add(makeUser("a@mail.ru", "auser"));
        userStorage.add(makeUser("b@mail.ru", "buser"));
        Collection<User> users = userStorage.findAll();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.add(makeUser("old@mail.ru", "oldlogin"));
        user.setEmail("new@mail.ru");
        userStorage.update(user);
        Optional<User> updated = userStorage.findById(user.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void testAddAndGetFriends() {
        User user1 = userStorage.add(makeUser("u1@mail.ru", "user1"));
        User user2 = userStorage.add(makeUser("u2@mail.ru", "user2"));
        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends.size()).isEqualTo(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
    }

    @Test
    void testFriendshipIsOneDirectional() {
        User user1 = userStorage.add(makeUser("dir1@mail.ru", "dir1"));
        User user2 = userStorage.add(makeUser("dir2@mail.ru", "dir2"));
        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> user2Friends = userStorage.getFriends(user2.getId());
        assertThat(user2Friends.size()).isEqualTo(0);
    }

    @Test
    void testRemoveFriend() {
        User user1 = userStorage.add(makeUser("rem1@mail.ru", "rem1"));
        User user2 = userStorage.add(makeUser("rem2@mail.ru", "rem2"));
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId()).size()).isEqualTo(0);
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.add(makeUser("cf1@mail.ru", "cf1"));
        User user2 = userStorage.add(makeUser("cf2@mail.ru", "cf2"));
        User common = userStorage.add(makeUser("cfc@mail.ru", "cfc"));
        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());
        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends.size()).isEqualTo(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(common.getId());
    }

    @Test
    void testFindFilmById() {
        Film film = filmStorage.add(makeFilm("Test Film", 1));
        Optional<Film> found = filmStorage.findById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Film");
        assertThat(found.get().getMpa().getId()).isEqualTo(1);
    }

    @Test
    void testFindAllFilms() {
        filmStorage.add(makeFilm("Film A", 1));
        filmStorage.add(makeFilm("Film B", 2));
        assertThat(filmStorage.findAll().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.add(makeFilm("Old Name", 1));
        film.setName("New Name");
        filmStorage.update(film);
        Optional<Film> updated = filmStorage.findById(film.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name");
    }

    @Test
    void testFilmWithGenres() {
        Film film = makeFilm("Genre Film", 1);
        film.setGenres(List.of(new Genre(1, null), new Genre(2, null)));
        Film saved = filmStorage.add(film);
        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres().size()).isEqualTo(2);
    }

    @Test
    void testAddAndRemoveLike() {
        User user = userStorage.add(makeUser("like@mail.ru", "likeuser"));
        Film film = filmStorage.add(makeFilm("Liked Film", 1));
        filmStorage.addLike(film.getId(), user.getId());
        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular.stream().anyMatch(f -> f.getId() == film.getId())).isTrue();
        filmStorage.removeLike(film.getId(), user.getId());
    }

    @Test
    void testGetPopular() {
        User user = userStorage.add(makeUser("pop@mail.ru", "popuser"));
        Film film1 = filmStorage.add(makeFilm("Popular Film", 1));
        Film film2 = filmStorage.add(makeFilm("Less Popular Film", 2));
        filmStorage.addLike(film1.getId(), user.getId());
        List<Film> popular = filmStorage.getPopular(1);
        assertThat(popular.size()).isEqualTo(1);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
    }

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.findAll();
        assertThat(genres.size()).isEqualTo(6);
    }

    @Test
    void testFindGenreById() {
        Optional<Genre> genre = genreStorage.findById(1);
        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void testGetAllMpa() {
        List<Mpa> ratings = mpaStorage.findAll();
        assertThat(ratings.size()).isEqualTo(5);
    }

    @Test
    void testFindMpaById() {
        Optional<Mpa> mpa = mpaStorage.findById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
    }
}
