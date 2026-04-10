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
import ru.practicum.shareit.exception.GlobalExceptionHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private ItemRequestDto sampleRequest() {
        return new ItemRequestDto(
                1L,
                "Хочу дрель",
                1L,
                LocalDateTime.now(),
                Collections.emptyList()
        );
    }

    // Позитивные тесты
    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto inputDto = new ItemRequestDto(null, "Хочу дрель", null, null, null);
        ItemRequestDto created = sampleRequest();

        when(requestService.create(any(ItemRequestDto.class), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.description").value("Хочу дрель"))
                .andExpect(jsonPath("$.requestorId").value(1L));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        ItemRequestDto created = sampleRequest();
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(created));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Хочу дрель"));
    }

    @Test
    void getAllOtherUsersRequests_shouldReturnList() throws Exception {
        ItemRequestDto created = sampleRequest();
        when(requestService.getAllRequests(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(created));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20"))
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

    // Негативные тесты
    @Test
    void createRequest_shouldReturnBadRequest_whenDescriptionEmpty() throws Exception {
        ItemRequestDto inputDto = new ItemRequestDto(null, "", null, null, null);
        when(requestService.create(any(ItemRequestDto.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Описание запроса не может быть пустым"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Описание запроса не может быть пустым"));
    }

    @Test
    void getRequestById_shouldReturnNotFound_whenRequestNotFound() throws Exception {
        when(requestService.getRequestById(999L, 1L))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос не найден"));
    }

    @Test
    void getAllRequests_shouldUseDefaultPagination() throws Exception {
        when(requestService.getAllRequests(eq(1L), eq(0), eq(20)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}
