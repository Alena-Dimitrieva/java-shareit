package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto dto, Long userId);
    List<ItemRequestDto> getAllRequests(Long userId, int from, int size);
    List<ItemRequestDto> getOwnRequests(Long userId);
    ItemRequestDto getRequestById(Long requestId, Long userId);
}