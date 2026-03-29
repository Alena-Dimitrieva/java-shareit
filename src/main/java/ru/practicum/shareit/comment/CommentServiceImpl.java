package ru.practicum.shareit.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {

        User user = UserMapper.toUser(userService.getUserById(userId));
        Item item = itemService.getItemById(itemId);

        List<Booking> bookings = bookingRepository.findByItem_IdAndBooker_Id(itemId, userId)
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .toList();

        boolean hasPastBooking = bookings.stream()
                .anyMatch(b -> b.getEnd().isBefore(LocalDateTime.now()));

        if (!hasPastBooking) {
            throw new IllegalArgumentException("Комментарий можно оставить только после окончания бронирования");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        comment = commentRepository.save(comment);

        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}