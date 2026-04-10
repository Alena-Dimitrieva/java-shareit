package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

@Component
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    public BookingClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> create(Long userId, BookingCreateDto bookingDto) {
        return postWithHeader(API_PREFIX, bookingDto, userId);
    }

    public ResponseEntity<Object> updateStatus(Long bookingId, Long userId, boolean approved) {
        String path = API_PREFIX + "/" + bookingId + "?approved=" + approved;
        return patchWithHeader(path, null, userId);
    }

    public ResponseEntity<Object> getById(Long bookingId, Long userId) {
        return getWithHeader(API_PREFIX + "/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByUser(Long userId, String state) {
        String path = API_PREFIX + "?state=" + state;
        return getWithHeader(path, userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, String state) {
        String path = API_PREFIX + "/owner?state=" + state;
        return getWithHeader(path, userId);
    }
}