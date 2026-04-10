package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }

        validateItemDto(itemDto);

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        Item item = ItemMapper.toItem(itemDto, request);
        item.setOwner(owner);

        return mapToDto(itemRepository.save(item), ownerId);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long ownerId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return mapToDto(itemRepository.save(item), ownerId);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        return mapToDto(item, userId);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> mapToDto(item, ownerId))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {

        if (text == null || text.isBlank()) return List.of();

        return itemRepository.searchAvailableByText(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }
    private ItemDto mapToDto(Item item, Long userId) {

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {

            lastBooking = bookingRepository
                    .findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(
                            item.getId(),
                            BookingStatus.APPROVED,
                            LocalDateTime.now()
                    )
                    .orElse(null);

            nextBooking = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(),
                            BookingStatus.APPROVED,
                            LocalDateTime.now()
                    )
                    .orElse(null);
        }

        List<CommentDto> comments = commentRepository
                .findByItemOrderByCreatedDesc(item)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    private void validateItemDto(ItemDto itemDto) {

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }
    }
}