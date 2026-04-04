package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateUserDto(userDto, null);
        User savedUser = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        if (userDto.getEmail() != null) {
            validateUserDto(userDto, id);
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        User savedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }

    private void validateUserDto(UserDto userDto, Long userIdBeingUpdated) {
        String email = userDto.getEmail();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email некорректен");
        }

        boolean duplicate = (userIdBeingUpdated == null)
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, userIdBeingUpdated);

        if (duplicate) {
            throw new EmailAlreadyExistsException("Email уже используется");
        }
    }
}