package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.add(user);
    }

    @Test
    void addAssignsIdAndFindByIdReturnsUser() {
        User user = createUser("first");

        assertThat(user.getId()).isPositive();
        assertThat(userStorage.findById(user.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getLogin()).isEqualTo("first"));
    }

    @Test
    void findByIdUnknownReturnsEmpty() {
        assertThat(userStorage.findById(9999)).isEmpty();
    }

    @Test
    void findAllReturnsCreatedUsers() {
        createUser("first");
        createUser("second");

        assertThat(userStorage.findAll()).hasSize(2);
    }

    @Test
    void updateChangesFields() {
        User user = createUser("first");
        user.setName("Обновлённое имя");

        userStorage.update(user);

        assertThat(userStorage.findById(user.getId()))
                .hasValueSatisfying(found -> assertThat(found.getName()).isEqualTo("Обновлённое имя"));
    }

    @Test
    void addFriendIsOneDirectional() {
        User user = createUser("first");
        User friend = createUser("second");

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void removeFriendDeletesFriendship() {
        User user = createUser("first");
        User friend = createUser("second");
        userStorage.addFriend(user.getId(), friend.getId());

        userStorage.removeFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void getCommonFriendsReturnsShared() {
        User first = createUser("first");
        User second = createUser("second");
        User common = createUser("common");
        userStorage.addFriend(first.getId(), common.getId());
        userStorage.addFriend(second.getId(), common.getId());

        assertThat(userStorage.getCommonFriends(first.getId(), second.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }
}
