package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.comment.dto.CommentDto;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemForRequestDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;
    private List<CommentDto> comments;
}