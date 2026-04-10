package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @Mock
    private BookingRepository bookingRepository;

    private BookingServiceImpl bookingService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        bookingService = new BookingServiceImpl(userService, itemService, bookingRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private UserDto bookerDto() {
        return new UserDto(1L, "booker@mail.com", "Booker");
    }

    private User owner() {
        return new User(2L, "owner@mail.com", "Owner");
    }

    private Item availableItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner());
        return item;
    }

    private BookingCreateDto validBooking() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setItemId(1L);
        return dto;
    }

    private Booking bookingEntity() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(availableItem());
        booking.setBooker(new User(1L, "booker@mail.com", "Booker"));
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    // Позитивные тесты

    @Test
    void createBooking_shouldCreateBooking_whenValid() {
        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());
        when(bookingRepository.save(any())).thenReturn(bookingEntity());

        BookingDto created = bookingService.create(validBooking(), 1L);

        assertNotNull(created.getId());
        assertEquals(BookingStatus.WAITING.name(), created.getStatus());
    }

    @Test
    void updateBooking_shouldApproveBooking() {
        Booking booking = bookingEntity();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto updated = bookingService.updateStatus(1L, 2L, true);

        assertEquals(BookingStatus.APPROVED.name(), updated.getStatus());
    }

    @Test
    void getBookingById_shouldReturnBooking() {
        Booking booking = bookingEntity();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto found = bookingService.getById(1L, 1L);

        assertEquals(1L, found.getId());
    }

    @Test
    void getAllByUser_shouldReturnBookings() {
        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(bookingRepository.findByBooker_IdOrderByStartDesc(1L))
                .thenReturn(List.of(bookingEntity(), bookingEntity()));

        List<BookingDto> bookings = bookingService.getAllByUser(1L, BookingState.ALL);

        assertEquals(2, bookings.size());
    }

    // Негативные тесты

    @Test
    void createBooking_shouldThrow_whenItemNotAvailable() {
        when(userService.getUserById(1L)).thenReturn(bookerDto());

        Item item = availableItem();
        item.setAvailable(false);

        when(itemService.getItemById(1L)).thenReturn(item);

        assertThrows(BookingValidationException.class,
                () -> bookingService.create(validBooking(), 1L));
    }

    @Test
    void createBooking_shouldThrow_whenBookingOwnItem() {
        when(userService.getUserById(2L)).thenReturn(new UserDto(2L, "owner@mail.com", "Owner"));
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        assertThrows(BookingValidationException.class,
                () -> bookingService.create(validBooking(), 2L));
    }

    @Test
    void updateBooking_shouldThrowForbidden_whenNotOwner() {
        Booking booking = bookingEntity();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenOperationException.class,
                () -> bookingService.updateStatus(1L, 999L, true));
    }

    @Test
    void getBookingById_shouldThrowForbidden() {
        Booking booking = bookingEntity();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenOperationException.class,
                () -> bookingService.getById(1L, 999L));
    }

    @Test
    void getBookingById_shouldThrowNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getById(1L, 1L));
    }
}
