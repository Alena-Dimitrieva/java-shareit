package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestBody BookingCreateDto bookingCreateDto,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (bookingCreateDto == null) {
            throw new BookingValidationException("BookingCreateDto не может быть null");
        }
        return bookingService.create(bookingCreateDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@PathVariable Long bookingId,
                                   @RequestParam boolean approved,
                                   @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.updateStatus(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        return bookingService.getAllByUser(userId, BookingState.from(stateParam));
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                          @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        return bookingService.getAllByOwner(ownerId, BookingState.from(stateParam));
    }
}