package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post(API_PREFIX, userDto);
    }

    public ResponseEntity<Object> update(Long id, UserDto userDto) {
        return patch(API_PREFIX + "/" + id, userDto);
    }

    public ResponseEntity<Object> getById(Long id) {
        return get(API_PREFIX + "/" + id);
    }

    public ResponseEntity<Object> getAll() {
        return get(API_PREFIX);
    }

    public ResponseEntity<Object> delete(Long id) {
        return delete(API_PREFIX + "/" + id);
    }
}