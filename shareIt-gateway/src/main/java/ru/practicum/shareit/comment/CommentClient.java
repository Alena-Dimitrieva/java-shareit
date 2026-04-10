package ru.practicum.shareit.comment;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.comment.dto.CommentDto;

@Component
public class CommentClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public CommentClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto dto) {
        return postWithHeader(API_PREFIX + "/" + itemId + "/comment", dto, userId);
    }
}