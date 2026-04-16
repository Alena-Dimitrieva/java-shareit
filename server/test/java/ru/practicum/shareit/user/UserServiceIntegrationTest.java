package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//Проверяет создание, обновление и поиск пользователя
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createAndGetUser_shouldWork() {
        UserDto newUser = new UserDto(null, "integ@test.com", "Integration User");
        UserDto created = userService.create(newUser);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("integ@test.com");

        UserDto found = userService.getUserById(created.getId());
        assertThat(found.getName()).isEqualTo("Integration User");
    }

    @Test
    void updateUser_shouldUpdateEmail() {
        UserDto user = userService.create(new UserDto(null, "old@mail.com", "Old"));
        UserDto update = new UserDto(null, "new@mail.com", null);

        UserDto updated = userService.update(user.getId(), update);
        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
        assertThat(updated.getName()).isEqualTo("Old"); // имя не менялось
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        UserDto user = userService.create(new UserDto(null, "delete@me.com", "Delete"));
        Long id = user.getId();

        userService.deleteUser(id);
        assertThrows(NotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    void getAllUsers_shouldReturnAll() {
        userService.create(new UserDto(null, "u1@t.com", "U1"));
        userService.create(new UserDto(null, "u2@t.com", "U2"));

        List<UserDto> all = userService.getAllUsers();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void createDuplicateEmail_shouldThrow() {
        userService.create(new UserDto(null, "duplicate@mail.com", "First"));
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.create(new UserDto(null, "duplicate@mail.com", "Second")));
    }
}
