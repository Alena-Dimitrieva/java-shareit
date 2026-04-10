package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//Проверяет полный цикл бронирования и фильтрацию по состояниям
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createAndApproveBooking() {
        UserDto owner = userService.create(new UserDto(null, "owner@test.com", "Owner"));
        UserDto booker = userService.create(new UserDto(null, "booker@test.com", "Booker"));

        ItemDto item = new ItemDto();
        item.setName("Вещь");
        item.setDescription("Описание");
        item.setAvailable(true);
        ItemDto savedItem = itemService.create(item, owner.getId());

        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(savedItem.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.create(createDto, booker.getId());
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING.name());

        BookingDto approved = bookingService.updateStatus(created.getId(), owner.getId(), true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED.name());
    }

    @Test
    void cannotBookOwnItem() {
        UserDto owner = userService.create(new UserDto(null, "owner@test.com", "Owner"));
        ItemDto item = new ItemDto();
        item.setName("Моя вещь");
        item.setDescription("...");
        item.setAvailable(true);
        ItemDto savedItem = itemService.create(item, owner.getId());

        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(savedItem.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(BookingValidationException.class,
                () -> bookingService.create(createDto, owner.getId()));
    }

    @Test
    void getAllByUserWithState() {
        UserDto owner = userService.create(new UserDto(null, "owner@test.com", "Owner"));
        UserDto booker = userService.create(new UserDto(null, "booker@test.com", "Booker"));

        ItemDto item = new ItemDto();
        item.setName("Вещь");
        item.setDescription("...");
        item.setAvailable(true);
        ItemDto savedItem = itemService.create(item, owner.getId());

        // будущее бронирование
        BookingCreateDto future = new BookingCreateDto();
        future.setItemId(savedItem.getId());
        future.setStart(LocalDateTime.now().plusDays(5));
        future.setEnd(LocalDateTime.now().plusDays(6));
        bookingService.create(future, booker.getId());

        // прошедшее бронирование
        BookingCreateDto past = new BookingCreateDto();
        past.setItemId(savedItem.getId());
        past.setStart(LocalDateTime.now().minusDays(5));
        past.setEnd(LocalDateTime.now().minusDays(2));
        var pastBooking = bookingService.create(past, booker.getId());
        bookingService.updateStatus(pastBooking.getId(), owner.getId(), true);

        List<BookingDto> futureList = bookingService.getAllByUser(booker.getId(), BookingState.FUTURE);
        assertThat(futureList).hasSize(1);

        List<BookingDto> pastList = bookingService.getAllByUser(booker.getId(), BookingState.PAST);
        assertThat(pastList).hasSize(1);
    }
}
