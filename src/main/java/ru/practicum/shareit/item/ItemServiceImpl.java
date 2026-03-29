package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.ForbiddenOperationException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }

        validateItemDto(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);

        return mapToDtoWithBookings(savedItem, ownerId);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long ownerId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

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

        return mapToDtoWithBookings(itemRepository.save(item), ownerId);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        return mapToDtoWithBookings(item, userId);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> mapToDtoWithBookings(item, ownerId)) // 👈 важно
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {

        if (text == null || text.isBlank()) return List.of();

        return itemRepository.searchAvailableByText(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(Long itemId) {

        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));
    }

    private void validateItemDto(ItemDto itemDto) {

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }
    }

    private ItemDto mapToDtoWithBookings(Item item, Long userId) {

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {

            lastBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                            item.getId(),
                            BookingStatus.APPROVED,
                            LocalDateTime.now()
                    ).orElse(null);

            nextBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(),
                            BookingStatus.APPROVED,
                            LocalDateTime.now()
                    ).orElse(null);
        }

        ItemShortDto shortDto = ItemMapper.toItemShortDto(item, lastBooking, nextBooking);

        ItemDto dto = ItemMapper.toItemDto(item);
        dto.setLastBooking(shortDto.getLastBooking());
        dto.setNextBooking(shortDto.getNextBooking());
        dto.setComments(shortDto.getComments());

        return dto;
    }
}