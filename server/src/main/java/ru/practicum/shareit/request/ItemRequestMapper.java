package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        List<ItemForRequestDto> itemDtos = items != null
                ? items.stream()
                .map(item -> new ItemForRequestDto(
                        item.getId(),
                        item.getName() != null ? item.getName() : "Unnamed item",
                        item.getDescription() != null ? item.getDescription() : "",
                        item.getAvailable() != null ? item.getAvailable() : false,
                        item.getOwner().getId(),
                        item.getComments() != null
                                ? item.getComments().stream()
                                .map(ItemRequestMapper::toCommentDto)
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                ))
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new ItemRequestDto(
                request.getId(),
                request.getDescription() != null ? request.getDescription() : "",
                request.getRequestor() != null ? request.getRequestor().getId() : null,
                request.getCreated() != null ? request.getCreated() : LocalDateTime.now(),
                itemDtos
        );
    }

    private static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText() != null ? comment.getText() : "",
                comment.getAuthor() != null ? comment.getAuthor().getName() : "Unknown",
                comment.getCreated() != null ? comment.getCreated() : LocalDateTime.now()
        );
    }
}