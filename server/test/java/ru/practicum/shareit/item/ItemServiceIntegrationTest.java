package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.comment.CommentService;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//Проверяет создание вещи, поиск, добавление комментария после бронирования
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CommentService commentService;

    @Test
    void createAndSearchItem() {
        UserDto owner = userService.create(new UserDto(null, "owner@test.com", "Owner"));
        ItemDto item = new ItemDto();
        item.setName("Отвёртка");
        item.setDescription("Крестовая");
        item.setAvailable(true);

        ItemDto created = itemService.create(item, owner.getId());

        List<ItemDto> found = itemService.search("отвёрт");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(created.getId());
    }

    @Test
    void addCommentAfterBooking() {

        UserDto owner = userService.create(new UserDto(null, "owner@test.com", "Owner"));
        UserDto booker = userService.create(new UserDto(null, "booker@test.com", "Booker"));
        ItemDto item = new ItemDto();
        item.setName("Дрель");
        item.setDescription("Мощная");
        item.setAvailable(true);
        ItemDto savedItem = itemService.create(item, owner.getId());
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(savedItem.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(5));
        bookingDto.setEnd(LocalDateTime.now().minusDays(2));
        var booking = bookingService.create(bookingDto, booker.getId());

        bookingService.updateStatus(booking.getId(), owner.getId(), true);

        CommentDto comment = new CommentDto();
        comment.setText("Отличная вещь!");
        CommentDto savedComment = commentService.addComment(booker.getId(), savedItem.getId(), comment);

        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("Отличная вещь!");
        assertThat(savedComment.getAuthorName()).isEqualTo("Booker");
    }
}
