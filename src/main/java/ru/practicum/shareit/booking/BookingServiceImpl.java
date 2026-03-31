package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.BookingValidationException;
import ru.practicum.shareit.Exception.ForbiddenOperationException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingRepository bookingRepository;

    @Override
    public BookingDto create(BookingCreateDto dto, Long bookerId) {

        if (dto == null) {
            throw new BookingValidationException("BookingCreateDto не может быть null");
        }

        if (dto.getItemId() == null) {
            throw new BookingValidationException("Не передан ID вещи");
        }

        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new BookingValidationException("Даты обязательны");
        }

        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new BookingValidationException("Некорректные даты");
        }

        User booker = UserMapper.toUser(userService.getUserById(bookerId));
        Item item = itemService.getItemById(dto.getItemId());

        if (!item.isAvailable()) {
            throw new BookingValidationException("Вещь недоступна");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new BookingValidationException("Нельзя бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto updateStatus(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking не найден"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("Нет прав");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingValidationException("Статус уже изменён");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        return bookingRepository.findById(bookingId)
                .map(b -> {
                    if (!b.getBooker().getId().equals(userId) &&
                            !b.getItem().getOwner().getId().equals(userId)) {
                        throw new ForbiddenOperationException("Нет доступа");
                    }
                    return BookingMapper.toBookingDto(b);
                })
                .orElseThrow(() -> new NoSuchElementException("Booking не найден"));
    }

    @Override
    public List<BookingDto> getAllByUser(Long userId, BookingState state) {
        userService.getUserById(userId);

        LocalDateTime now = LocalDateTime.now();
        if (state == null) state = BookingState.ALL;

        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);

            case PAST -> bookingRepository
                    .findByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);

            case FUTURE -> bookingRepository
                    .findByBooker_IdAndStartAfterOrderByStartDesc(userId, now);

            case WAITING -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);

            case REJECTED -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);

            case ALL -> bookingRepository
                    .findByBooker_IdOrderByStartDesc(userId);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId, BookingState state) {
        userService.getUserById(ownerId);

        LocalDateTime now = LocalDateTime.now();
        if (state == null) state = BookingState.ALL;

        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now);

            case PAST -> bookingRepository
                    .findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now);

            case FUTURE -> bookingRepository
                    .findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now);

            case WAITING -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);

            case REJECTED -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);

            case ALL -> bookingRepository
                    .findByItem_Owner_IdOrderByStartDesc(ownerId);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}