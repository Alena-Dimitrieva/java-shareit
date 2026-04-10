package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ItemRequestClient itemRequestClient;

    @Test
    void create_shouldCallExchange() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = itemRequestClient.create(1L, dto);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getAll_shouldCallExchange() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = itemRequestClient.getAll(1L, 0, 20);
        assertThat(response).isEqualTo(expected);
    }
}
