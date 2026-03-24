package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public UserDto create(UserDto userDto) {
        validateUserDto(userDto, null);

        User user = UserMapper.toUser(userDto);
        user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = users.get(id);
        if (existingUser == null) throw new NoSuchElementException("Пользователь не найден");
        if (userDto.getEmail() != null) {
            validateUserDto(userDto, id);
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) existingUser.setName(userDto.getName());

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = users.get(id);
        if (user == null) throw new NoSuchElementException("Пользователь не найден");
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public void deleteUser(Long id) {
        if (!users.containsKey(id)) throw new NoSuchElementException("Пользователь не найден");
        users.remove(id);
    }

    private void validateUserDto(UserDto userDto, Long userIdBeingUpdated) {
        String email = userDto.getEmail();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email некорректен");
        }
        // Проверка уникальности email
        boolean duplicate = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email)
                        && !Objects.equals(u.getId(), userIdBeingUpdated));

        if (duplicate) {
            throw new EmailAlreadyExistsException("Email уже используется");
        }
    }
}