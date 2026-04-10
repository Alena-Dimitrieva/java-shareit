package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item,
                                    Booking lastBooking,
                                    Booking nextBooking,
                                    List<CommentDto> comments) {

        if (item == null) return null;

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                comments != null ? comments : List.of(),
                lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null,
                nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null
        );
    }

    public static ItemDto toItemDto(Item item) {
        return toItemDto(item, null, null, List.of());
    }

    public static Item toItem(ItemDto dto, ItemRequest request) {

        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setRequest(request);

        return item;
    }
}