package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createRequest_shouldCallClient() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        when(itemRequestClient.create(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createRequest_shouldReturnBadRequest_whenDescriptionBlank() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldUseDefaultPagination() throws Exception {
        when(itemRequestClient.getAll(eq(1L), eq(0), eq(20)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldPassPaginationParams() throws Exception {
        when(itemRequestClient.getAll(eq(1L), eq(5), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwn_shouldCallClient() throws Exception {
        when(itemRequestClient.getOwn(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldCallClient() throws Exception {
        when(itemRequestClient.getById(1L, 1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}
