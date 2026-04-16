package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingCreateDto, Long bookerId);

    BookingDto updateStatus(Long bookingId, Long ownerId, boolean approved);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAllByUser(Long userId, BookingState state);

    List<BookingDto> getAllByOwner(Long ownerId, BookingState state);
}