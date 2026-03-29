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

        if (!booking.getItem().getOwner().getId().equals(ownerId))
            throw new ForbiddenOperationException("Нет прав");

        if (booking.getStatus() != BookingStatus.WAITING)
            throw new BookingValidationException("Статус уже изменён");

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        return bookingRepository.findById(bookingId)
                .map(b -> {
                    if (!b.getBooker().getId().equals(userId) && !b.getItem().getOwner().getId().equals(userId))
                        throw new ForbiddenOperationException("Нет доступа");
                    return BookingMapper.toBookingDto(b);
                })
                .orElseThrow(() -> new NoSuchElementException("Booking не найден"));
    }

    @Override
    public List<BookingDto> getAllByUser(Long userId, BookingState state) {
        userService.getUserById(userId);

        return bookingRepository.findByBooker_Id(userId).stream()
                .filter(b -> filterByState(b, state))
                .sorted((a, b) -> b.getStart().compareTo(a.getStart()))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId, BookingState state) {
        userService.getUserById(ownerId);

        return bookingRepository.findByItem_Owner_Id(ownerId).stream()
                .filter(b -> filterByState(b, state))
                .sorted((a, b) -> b.getStart().compareTo(a.getStart()))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private boolean filterByState(Booking booking, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        if (state == null) state = BookingState.ALL;

        return switch (state) {
            case CURRENT -> !booking.getStart().isAfter(now) && !booking.getEnd().isBefore(now);
            case PAST -> booking.getEnd().isBefore(now);
            case FUTURE -> booking.getStart().isAfter(now);
            case WAITING -> booking.getStatus() == BookingStatus.WAITING;
            case REJECTED -> booking.getStatus() == BookingStatus.REJECTED;
            case ALL -> true;
        };
    }
}