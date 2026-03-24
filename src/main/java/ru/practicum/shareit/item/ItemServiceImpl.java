package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.Exception.ForbiddenOperationException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь не найдена");
        }
        return item;
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {

        UserDto ownerDto = userService.getUserById(ownerId);
        User owner = new User(ownerDto.getId(), ownerDto.getName(), ownerDto.getEmail());

        // Проверка available
        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }

        validateItemDto(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setId(idGenerator.getAndIncrement());
        item.setOwner(owner);

        items.put(item.getId(), item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long ownerId) {

        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь не найдена");
        }

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

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {

        userService.getUserById(userId);

        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь не найдена");
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {

        userService.getUserById(ownerId);

        return items.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lower = text.toLowerCase();

        return items.values().stream()
                .filter(Item::isAvailable)
                .filter(i -> i.getName().toLowerCase().contains(lower)
                        || i.getDescription().toLowerCase().contains(lower))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
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