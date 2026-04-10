package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateForCreate(userDto);

        User user = UserMapper.toUser(userDto);
        User saved = userRepository.save(user);

        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail(), id);
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }

    private void validateForCreate(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Имя обязательно");
        }

        validateEmail(dto.getEmail(), null);
    }

    private void validateEmail(String email, Long id) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email некорректен");
        }

        boolean exists = (id == null)
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, id);

        if (exists) {
            throw new EmailAlreadyExistsException("Email уже используется");
        }
    }
}