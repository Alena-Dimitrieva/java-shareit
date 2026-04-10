package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ItemClient itemClient;

    @Test
    void create_shouldPostWithHeader() {
        ItemDto dto = new ItemDto();
        dto.setName("Test");
        ResponseEntity<Object> expected = ResponseEntity.ok().build();

        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = itemClient.create(1L, dto);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getById_shouldGetWithHeader() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = itemClient.getById(1L, 1L);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void search_shouldEncodeText() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = itemClient.search("%D0%B4%D1%80%D0%B5%D0%BB%D1%8C");
        assertThat(response).isEqualTo(expected);
    }
}