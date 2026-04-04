package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

/**
 * Конвертация между User и UserDto
 */
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public static User toUser(UserDto dto) {
        return new User(
                dto.getId(),
                dto.getEmail(),
                dto.getName()
        );
    }
}