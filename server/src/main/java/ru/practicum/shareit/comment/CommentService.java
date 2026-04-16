package ru.practicum.shareit.comment;

import ru.practicum.shareit.comment.dto.CommentDto;

public interface CommentService {

    /**
     * Добавление комментария к вещи.
     */
    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
