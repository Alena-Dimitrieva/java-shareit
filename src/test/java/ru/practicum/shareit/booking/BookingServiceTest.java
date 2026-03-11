package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.Exception.ForbiddenOperationException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;
    private BookingServiceImpl bookingService;
    AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        bookingService = new BookingServiceImpl(userService, itemService);
    }

    private UserDto bookerDto() {
        return new UserDto(1L, "Booker", "booker@mail.com");
    }

    private UserDto ownerDto() {
        return new UserDto(2L, "Owner", "owner@mail.com");
    }

    private Item availableItem() {
        Item item = new Item(1L, "Drill", "Power drill", true, null, null);
        item.setOwner(new ru.practicum.shareit.user.User(2L, "Owner", "owner@mail.com"));
        return item;
    }

    private BookingDto validBooking() {
        return new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                null
        );
    }

    //Тесты с позитивным сценарием
    @Test
    void createBooking_shouldCreateBooking_whenDataValid() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        BookingDto created = bookingService.create(validBooking(), 1L);

        assertNotNull(created.getId());
        assertEquals("WAITING", created.getStatus());
        assertEquals(1L, created.getItemId());
        assertEquals(1L, created.getBookerId());
    }

    @Test
    void updateBooking_shouldApproveBooking_whenOwnerApproves() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        BookingDto created = bookingService.create(validBooking(), 1L);

        BookingDto updated = bookingService.updateStatus(created.getId(), 2L, true);

        assertEquals("APPROVED", updated.getStatus());
    }

    @Test
    void getBookingById_shouldReturnBooking() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        BookingDto created = bookingService.create(validBooking(), 1L);

        BookingDto found = bookingService.getById(created.getId(), 1L);

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getAllByUser_shouldReturnUserBookings() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        bookingService.create(validBooking(), 1L);
        bookingService.create(validBooking(), 1L);

        List<BookingDto> bookings = bookingService.getAllByUser(1L);

        assertEquals(2, bookings.size());
    }

    //Тесты с негативным сценарием
    @Test
    void createBooking_shouldThrowException_whenItemNotAvailable() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        Item item = availableItem();
        item.setAvailable(false);
        when(itemService.getItemById(1L)).thenReturn(item);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(validBooking(), 1L));
    }

    @Test
    void createBooking_shouldThrowException_whenBookingOwnItem() {

        when(userService.getUserById(2L)).thenReturn(ownerDto());
        Item item = availableItem();
        when(itemService.getItemById(1L)).thenReturn(item);

        BookingDto booking = validBooking();
        booking.setBookerId(2L);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(booking, 2L));
    }

    @Test
    void updateBooking_shouldThrowForbidden_whenNotOwner() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        BookingDto created = bookingService.create(validBooking(), 1L);

        assertThrows(ForbiddenOperationException.class,
                () -> bookingService.updateStatus(created.getId(), 999L, true));
    }

    @Test
    void getBookingById_shouldThrowForbidden_whenUserNotBookerOrOwner() {

        when(userService.getUserById(1L)).thenReturn(bookerDto());
        when(itemService.getItemById(1L)).thenReturn(availableItem());

        BookingDto created = bookingService.create(validBooking(), 1L);

        assertThrows(ForbiddenOperationException.class,
                () -> bookingService.getById(created.getId(), 999L));
    }

    @Test
    void getBookingById_shouldThrowNotFound_whenBookingNotExists() {

        assertThrows(NoSuchElementException.class,
                () -> bookingService.getById(999L, 1L));
    }
}
