package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.booking.dto.UserShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) return null;

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());

        if (booking.getItem() != null) {
            ItemShortDto itemDto = new ItemShortDto(
                    booking.getItem().getId(),
                    booking.getItem().getName(),
                    null,
                    null,
                    null
            );
            dto.setItem(itemDto);
        }

        if (booking.getBooker() != null) {
            UserShortDto userDto = new UserShortDto();
            userDto.setId(booking.getBooker().getId());
            dto.setBooker(userDto);
        }

        dto.setStatus(booking.getStatus().name());

        return dto;
    }

    public static Booking toBooking(BookingCreateDto dto, Item item, User booker) {
        if (dto == null) return null;

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }
}