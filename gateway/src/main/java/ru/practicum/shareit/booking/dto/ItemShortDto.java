package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.comment.dto.CommentDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemShortDto {
    private Long id;
    private String name;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
}
