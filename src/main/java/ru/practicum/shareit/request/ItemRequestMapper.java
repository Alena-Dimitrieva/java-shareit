package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequestor() != null ? request.getRequestor().getId() : null,
                request.getCreated()
        );
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requestor) {
        return new ItemRequest(
                dto.getId(),
                dto.getDescription(),
                requestor,
                LocalDateTime.now()
        );
    }
}