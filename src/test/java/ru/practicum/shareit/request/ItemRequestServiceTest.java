package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    private ItemRequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestService = new ItemRequestServiceImpl(userRepository, requestRepository);
    }

    private User owner() {
        return new User(1L, "Alice", "alice@mail.com");
    }

    private ItemRequestDto validRequest() {
        return new ItemRequestDto(null, "Нужна дрель", 1L, null);
    }

    // Позитивные тесты
    @Test
    void createRequest_shouldCreateRequest_whenDataValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));

        when(requestRepository.save(any())).thenAnswer(invocation -> {
            ItemRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        ItemRequestDto created = requestService.create(validRequest(), 1L);

        assertNotNull(created.getId());
        assertEquals("Нужна дрель", created.getDescription());
    }

    @Test
    void getAllRequests_shouldReturnRequestsFromOtherUsers() {
        User user1 = owner();
        User user2 = new User(2L, "Bob", "bob@mail.com");

        ItemRequest req = new ItemRequest();
        req.setId(1L);
        req.setDescription("Нужна дрель");
        req.setRequestor(user1);

        when(requestRepository.findAll()).thenReturn(List.of(req));

        List<ItemRequestDto> requests = requestService.getAllRequests(2L);

        assertEquals(1, requests.size());
    }

    @Test
    void getRequestById_shouldReturnRequest() {
        ItemRequest req = new ItemRequest();
        req.setId(1L);
        req.setDescription("Нужна дрель");
        req.setRequestor(owner());

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));

        ItemRequestDto found = requestService.getRequestById(1L, 1L);

        assertEquals(1L, found.getId());
    }

    // Негативные тесты
    @Test
    void createRequest_shouldThrowException_whenDescriptionEmpty() {
        ItemRequestDto request = new ItemRequestDto(null, "", 1L, null);

        assertThrows(IllegalArgumentException.class,
                () -> requestService.create(request, 1L));
    }

    @Test
    void getRequestById_shouldThrowException_whenRequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> requestService.getRequestById(999L, 1L));
    }
}