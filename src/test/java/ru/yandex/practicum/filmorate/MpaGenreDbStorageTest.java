package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaGenreDbStorageTest {
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    @Test
    void findAllMpaReturnsFiveRatings() {
        assertThat(mpaStorage.findAll())
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void findMpaByIdReturnsRating() {
        assertThat(mpaStorage.findById(5))
                .hasValueSatisfying(mpa -> assertThat(mpa.getName()).isEqualTo("NC-17"));
    }

    @Test
    void findMpaByUnknownIdReturnsEmpty() {
        assertThat(mpaStorage.findById(9999)).isEmpty();
    }

    @Test
    void findAllGenresReturnsSixGenres() {
        assertThat(genreStorage.findAll())
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void findGenreByIdReturnsGenre() {
        assertThat(genreStorage.findById(1))
                .hasValueSatisfying(genre -> assertThat(genre.getName()).isEqualTo("Комедия"));
    }

    @Test
    void findGenreByUnknownIdReturnsEmpty() {
        assertThat(genreStorage.findById(9999)).isEmpty();
    }

    @Test
    void findGenresByIdsReturnsMatchingGenresSorted() {
        assertThat(genreStorage.findByIds(List.of(3, 1)))
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Мультфильм");
    }

    @Test
    void findGenresByIdsSkipsUnknownIds() {
        assertThat(genreStorage.findByIds(List.of(1, 9999)))
                .extracting(Genre::getId)
                .containsExactly(1);
    }
}
