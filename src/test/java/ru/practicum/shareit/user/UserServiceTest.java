package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.Exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
    }

    //Тесты с позитивным сценарием
    @Test
    void shouldCreateUser() {
        UserDto user = new UserDto(null, "Alice", "alice@mail.com");

        UserDto created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals("Alice", created.getName());
        assertEquals("alice@mail.com", created.getEmail());
    }

    @Test
    void shouldGetUserById() {
        UserDto created = userService.create(new UserDto(null, "Bob", "bob@mail.com"));

        UserDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Bob", found.getName());
    }

    @Test
    void shouldUpdateUserName() {
        UserDto created = userService.create(new UserDto(null, "Charlie", "charlie@mail.com"));

        UserDto updateDto = new UserDto(null, "CharlieUpdated", null);
        UserDto updated = userService.update(created.getId(), updateDto);

        assertEquals("CharlieUpdated", updated.getName());
        assertEquals("charlie@mail.com", updated.getEmail());
    }

    @Test
    void shouldReturnAllUsers() {
        userService.create(new UserDto(null, "User1", "user1@mail.com"));
        userService.create(new UserDto(null, "User2", "user2@mail.com"));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void shouldDeleteUser() {
        UserDto created = userService.create(new UserDto(null, "DeleteMe", "delete@mail.com"));

        userService.deleteUser(created.getId());

        assertThrows(NoSuchElementException.class,
                () -> userService.getUserById(created.getId()));
    }

    //Тесты с негативным сценарием
    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        UserDto user = new UserDto(null, "Alice", "");

        assertThrows(IllegalArgumentException.class,
                () -> userService.create(user));
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        UserDto user = new UserDto(null, "Alice", "invalidEmail");

        assertThrows(IllegalArgumentException.class,
                () -> userService.create(user));
    }

    @Test
    void shouldThrowExceptionForDuplicateEmail() {
        userService.create(new UserDto(null, "User1", "duplicate@mail.com"));

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.create(new UserDto(null, "User2", "duplicate@mail.com")));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {
        assertThrows(NoSuchElementException.class,
                () -> userService.deleteUser(999L));
    }
}
