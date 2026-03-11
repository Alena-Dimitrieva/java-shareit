package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.Exception.GlobalExceptionHandler;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemRequestControllerTest {

    @Mock
    private ItemRequestService requestService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        ItemRequestController requestController = new ItemRequestController(requestService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // важно для LocalDateTime
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private ItemRequestDto sampleRequest() {
        return new ItemRequestDto(1L, "Хочу дрель", 1L, LocalDateTime.now());
    }

    //Тесты с позитивным сценарием
    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(null, "Хочу дрель", 1L, null);
        ItemRequestDto created = sampleRequest();

        when(requestService.create(any(), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.description").value("Хочу дрель"))
                .andExpect(jsonPath("$.requestorId").value(1L));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        ItemRequestDto created = sampleRequest();
        when(requestService.getAllRequests(1L)).thenReturn(Collections.singletonList(created));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Хочу дрель"));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        ItemRequestDto created = sampleRequest();
        when(requestService.getRequestById(1L, 1L)).thenReturn(created);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Хочу дрель"));
    }

    //Тесты с негативным сценарием
    @Test
    void createRequest_shouldReturnBadRequest_whenDescriptionEmpty() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(null, "", 1L, null);
        when(requestService.create(any(), eq(1L)))
                .thenThrow(new IllegalArgumentException("Описание запроса не может быть пустым"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Описание запроса не может быть пустым"));
    }

    @Test
    void getRequestById_shouldReturnNotFound_whenRequestNotFound() throws Exception {
        when(requestService.getRequestById(999L, 1L))
                .thenThrow(new NoSuchElementException("Запрос не найден"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос не найден"));
    }
}