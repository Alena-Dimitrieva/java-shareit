package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemRequestDto dto) {
        return service.create(dto, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllOtherUsersRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "20") int size) {
        return service.getAllRequests(userId, from, size);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getOwnRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId) {
        return service.getRequestById(requestId, userId);
    }
}