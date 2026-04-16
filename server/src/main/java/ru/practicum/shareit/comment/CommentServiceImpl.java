package ru.practicum.shareit.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

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

        LocalDateTime now = LocalDateTime.now();

        boolean hasFinishedBooking = bookingRepository
                .findByItem_IdAndBooker_Id(itemId, userId)
                .stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED
                        && b.getEnd().isBefore(now));  // без буфера

        if (!hasFinishedBooking) {
            throw new BookingValidationException(
                    "Комментарий можно оставить только после окончания бронирования"
            );
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(now);

        comment = commentRepository.save(comment);

        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}