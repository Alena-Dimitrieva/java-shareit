package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking) {
        if (item == null) return null;

        List<CommentDto> comments = item.getComments() != null
                ? item.getComments().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList())
                : List.of();

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                comments,
                lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null,
                nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null
        );
    }

    public static ItemDto toItemDto(Item item) {
        return toItemDto(item, null, null);
    }

    public static Item toItem(ItemDto dto) {
        if (dto == null) return null;

        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        item.setAvailable(dto.getAvailable() != null && dto.getAvailable());

        return item;
    }

    public static ItemShortDto toItemShortDto(Item item, Booking lastBooking, Booking nextBooking) {
        if (item == null) return null;

        List<CommentDto> comments = item.getComments() != null
                ? item.getComments().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList())
                : List.of();

        return new ItemShortDto(
                item.getId(),
                item.getName(),
                lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null,
                nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null,
                comments
        );
    }
}