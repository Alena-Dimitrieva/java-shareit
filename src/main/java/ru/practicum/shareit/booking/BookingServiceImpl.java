package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.ForbiddenOperationException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserService userService;
    private final ItemService itemService;

    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public BookingDto create(BookingDto bookingDto, Long bookerId) {

        User booker = UserMapper.toUser(userService.getUserById(bookerId));

        validateBookingDates(bookingDto);

        Item item = itemService.getItemById(bookingDto.getItemId());

        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new IllegalArgumentException("Нельзя бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setId(idGenerator.getAndIncrement());
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        bookings.put(booking.getId(), booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto updateStatus(Long bookingId, Long ownerId, boolean approved) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            throw new NoSuchElementException("Бронирование не найдено");
        }

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("Только владелец может менять статус бронирования");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalStateException("Статус бронирования уже изменён");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {

        userService.getUserById(userId);

        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            throw new NoSuchElementException("Бронирование не найдено");
        }

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException("Доступ запрещён");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByUser(Long userId) {

        userService.getUserById(userId);

        return bookings.values().stream()
                .filter(b -> b.getBooker().getId().equals(userId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId) {

        userService.getUserById(ownerId);

        return bookings.values().stream()
                .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void validateBookingDates(BookingDto bookingDto) {

        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Дата начала и окончания бронирования обязательны");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата начала бронирования не может быть в прошлом");
        }
    }
}