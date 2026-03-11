package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto bookingDto, Long bookerId);
    BookingDto updateStatus(Long bookingId, Long ownerId, boolean approved);
    BookingDto getById(Long bookingId, Long userId);
    List<BookingDto> getAllByUser(Long userId);
    List<BookingDto> getAllByOwner(Long ownerId);
}