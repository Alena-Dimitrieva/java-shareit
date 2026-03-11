package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ItemRequestServiceTest {

    @Mock
    private UserService userService;

    private ItemRequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestService = new ItemRequestServiceImpl(userService);
    }

    private UserDto owner() {
        return new UserDto(1L, "Alice", "alice@mail.com");
    }

    private ItemRequestDto validRequest() {
        return new ItemRequestDto(null, "Нужна дрель", 1L, null);
    }

    //Тесты с позитивным сценарием
    @Test
    void createRequest_shouldCreateRequest_whenDataValid() {
        when(userService.getUserById(1L)).thenReturn(owner());

        ItemRequestDto created = requestService.create(validRequest(), 1L);

        assertNotNull(created.getId());
        assertEquals("Нужна дрель", created.getDescription());
    }

    @Test
    void getAllRequests_shouldReturnRequestsFromOtherUsers() {
        when(userService.getUserById(1L)).thenReturn(owner());

        requestService.create(validRequest(), 1L);

        List<ItemRequestDto> requests = requestService.getAllRequests(2L);

        assertEquals(1, requests.size());
    }

    @Test
    void getRequestById_shouldReturnRequest() {
        when(userService.getUserById(1L)).thenReturn(owner());

        ItemRequestDto created = requestService.create(validRequest(), 1L);

        ItemRequestDto found = requestService.getRequestById(created.getId(), 1L);

        assertEquals(created.getId(), found.getId());
    }

    //Тесты с негативным сценарием
    @Test
    void createRequest_shouldThrowException_whenDescriptionEmpty() {
        ItemRequestDto request = new ItemRequestDto(null, "", 1L, null);

        assertThrows(IllegalArgumentException.class,
                () -> requestService.create(request, 1L));
    }

    @Test
    void getRequestById_shouldThrowException_whenRequestNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> requestService.getRequestById(999L, 1L));
    }
}