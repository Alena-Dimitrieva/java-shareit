package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserService userService;
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ItemRequestServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание запроса не может быть пустым");
        }

        UserDto userDto = userService.getUserById(userId);
        User user = new User(userDto.getId(), userDto.getName(), userDto.getEmail());

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        request.setId(idGenerator.getAndIncrement());
        requests.put(request.getId(), request);

        return ItemRequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        return requests.values().stream()
                .filter(r -> !r.getRequestor().getId().equals(userId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        ItemRequest request = requests.get(requestId);
        if (request == null) throw new NoSuchElementException("Запрос не найден");
        return ItemRequestMapper.toDto(request);
    }
}