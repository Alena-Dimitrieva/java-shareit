package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingClient bookingClient;

    @Test
    void create_shouldCallExchange() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = bookingClient.create(1L, dto);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void updateStatus_shouldCallExchange() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = bookingClient.updateStatus(1L, 1L, true);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getById_shouldCallExchange() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = bookingClient.getById(1L, 1L);
        assertThat(response).isEqualTo(expected);
    }
}
