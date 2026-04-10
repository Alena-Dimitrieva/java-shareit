package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createUser_shouldReturnOk_whenValidInput() throws Exception {
        UserDto dto = new UserDto(null, "test@mail.com", "Test");
        when(userClient.create(any(UserDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
        UserDto dto = new UserDto(null, "invalid-email", "Test");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenNameBlank() throws Exception {
        UserDto dto = new UserDto(null, "test@mail.com", "");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldCallClient() throws Exception {
        UserDto dto = new UserDto(null, "update@mail.com", "Updated");
        when(userClient.update(eq(1L), any(UserDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_shouldCallClient() throws Exception {
        when(userClient.getById(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_shouldCallClient() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldCallClient() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}
