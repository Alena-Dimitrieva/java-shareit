package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Component
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto dto) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Missing X-Sharer-User-Id header");
        }
        return postWithHeader(API_PREFIX, dto, userId);
    }

    public ResponseEntity<Object> getAll(Long userId, int from, int size) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Missing X-Sharer-User-Id header");
        }
        String path = API_PREFIX + "/all?from=" + from + "&size=" + size;
        return getWithHeader(path, userId);
    }

    public ResponseEntity<Object> getOwn(Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Missing X-Sharer-User-Id header");
        }
        return getWithHeader(API_PREFIX, userId);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Missing X-Sharer-User-Id header");
        }
        return getWithHeader(API_PREFIX + "/" + requestId, userId);
    }
}