package ru.practicum.shareit.comment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class CommentServiceTest {

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    private CommentServiceImpl commentService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        commentService = new CommentServiceImpl(itemService, userService, bookingRepository, commentRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private UserDto userDto() {
        return new UserDto(1L, "alice@mail.com", "Alice");
    }

    private User user() {
        return new User(1L, "alice@mail.com", "Alice");
    }

    private Item item() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(new User(2L, "bob@mail.com", "Bob"));
        return item;
    }

    private Booking pastApprovedBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item());
        booking.setBooker(user());
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    private CommentDto commentDto() {
        CommentDto dto = new CommentDto();
        dto.setText("Great item!");
        return dto;
    }

    @Test
    void addComment_shouldAddCommentSuccessfully() {
        when(userService.getUserById(1L)).thenReturn(userDto());
        when(itemService.getItemById(1L)).thenReturn(item());
        when(bookingRepository.findByItem_IdAndBooker_Id(1L, 1L))
                .thenReturn(List.of(pastApprovedBooking()));

        Comment savedComment = new Comment(
                1L,
                "Great item!",
                item(),
                user(),
                LocalDateTime.now()
        );

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = commentService.addComment(1L, 1L, commentDto());

        assertNotNull(result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals("Alice", result.getAuthorName());
    }

    @Test
    void addComment_shouldThrow_whenNoPastBooking() {
        when(userService.getUserById(1L)).thenReturn(userDto());
        when(itemService.getItemById(1L)).thenReturn(item());
        when(bookingRepository.findByItem_IdAndBooker_Id(1L, 1L))
                .thenReturn(List.of()); // нет бронирований

        assertThrows(BookingValidationException.class,
                () -> commentService.addComment(1L, 1L, commentDto()));
    }
}
