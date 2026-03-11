package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.Exception.GlobalExceptionHandler;
import ru.practicum.shareit.Exception.ForbiddenOperationException;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;
    BookingController bookingController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        bookingController = new BookingController(bookingService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private BookingDto validBookingDto() {
        return new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                "WAITING"
        );
    }

    // Тесты с позитивным сценарием
    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        BookingDto dto = validBookingDto();
        when(bookingService.create(any(), eq(1L))).thenReturn(dto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        BookingDto dto = validBookingDto();
        when(bookingService.getById(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getAllByUser_shouldReturnList() throws Exception {
        BookingDto dto = validBookingDto();
        when(bookingService.getAllByUser(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()));
    }

    @Test
    void getAllByOwner_shouldReturnList() throws Exception {
        BookingDto dto = validBookingDto();
        when(bookingService.getAllByOwner(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()));
    }

    @Test
    void updateStatus_shouldReturnUpdatedBooking() throws Exception {
        BookingDto dto = validBookingDto();
        dto.setStatus("APPROVED");
        when(bookingService.updateStatus(1L, 1L, true)).thenReturn(dto);

        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // Тесты с негативным сценарием
    @Test
    void getBookingById_shouldReturnNotFound_whenBookingNotFound() throws Exception {
        when(bookingService.getById(999L, 1L))
                .thenThrow(new NoSuchElementException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenDatesInvalid() throws Exception {
        BookingDto dto = validBookingDto();
        dto.setStart(LocalDateTime.now().minusDays(1));
        dto.setEnd(LocalDateTime.now().minusDays(2));

        when(bookingService.create(any(), eq(1L)))
                .thenThrow(new IllegalArgumentException("Дата начала должна быть раньше даты окончания"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата начала должна быть раньше даты окончания"));
    }

    @Test
    void updateStatus_shouldReturnForbidden_whenNotOwner() throws Exception {
        when(bookingService.updateStatus(1L, 999L, true))
                .thenThrow(new ForbiddenOperationException("Только владелец может менять статус бронирования"));

        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Только владелец может менять статус бронирования"));
    }
}
