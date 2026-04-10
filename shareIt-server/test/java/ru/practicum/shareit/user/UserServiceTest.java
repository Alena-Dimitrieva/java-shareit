package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void shouldCreateUser() {
        User user = new User(1L, "alice@mail.com", "Alice");

        when(userRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.create(new UserDto(null, "alice@mail.com", "Alice"));

        assertNotNull(created.getId());
        assertEquals("Alice", created.getName());
        assertEquals("alice@mail.com", created.getEmail());
    }

    @Test
    void shouldGetUserById() {
        User user = new User(1L, "bob@mail.com", "Bob");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto found = userService.getUserById(1L);

        assertEquals(1L, found.getId());
        assertEquals("Bob", found.getName());
        assertEquals("bob@mail.com", found.getEmail());
    }

    @Test
    void shouldUpdateUserName() {
        User user = new User(1L, "charlie@mail.com", "Charlie");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("charlie@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto updated = userService.update(1L, new UserDto(null, null, "CharlieUpdated"));

        assertEquals("CharlieUpdated", updated.getName());
        assertEquals("charlie@mail.com", updated.getEmail());
    }

    @Test
    void shouldThrowExceptionForDuplicateEmail() {
        when(userRepository.existsByEmail("duplicate@mail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.create(new UserDto(null, "duplicate@mail.com", "User2")));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
    }
}
