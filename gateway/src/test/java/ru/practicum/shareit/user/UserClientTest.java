package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserClient userClient;

    @Test
    void create_shouldPostToCorrectUrl() {
        UserDto dto = new UserDto(null, "test@mail.com", "Test");
        ResponseEntity<Object> expected = ResponseEntity.ok().build();

        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = userClient.create(dto);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getById_shouldGetFromCorrectUrl() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = userClient.getById(1L);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getAll_shouldGetAll() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = userClient.getAll();
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void delete_shouldSendDeleteRequest() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = userClient.delete(1L);
        assertThat(response).isEqualTo(expected);
    }
}