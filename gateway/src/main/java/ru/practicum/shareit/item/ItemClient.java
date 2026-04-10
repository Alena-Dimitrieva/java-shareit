package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.ItemDto;

@Component
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> create(Long userId, ItemDto dto) {
        return postWithHeader(API_PREFIX, dto, userId);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, ItemDto dto) {
        return patchWithHeader(API_PREFIX + "/" + itemId, dto, userId);
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        return getWithHeader(API_PREFIX + "/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId) {
        return getWithHeader(API_PREFIX, userId);
    }

    public ResponseEntity<Object> search(String text) {
        return getWithParam(API_PREFIX + "/search?text=" + text);
    }
}