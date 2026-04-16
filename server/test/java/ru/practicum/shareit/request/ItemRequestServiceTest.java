package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ItemRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    private ItemRequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestService = new ItemRequestServiceImpl(userRepository, requestRepository, itemRepository);
    }

    private User requestor() {
        return new User(1L, "alice@mail.com", "Alice");
    }

    private ItemRequest validItemRequest() {
        return ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestor(requestor())
                .created(LocalDateTime.now())
                .build();
    }

    private ItemRequestDto validRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");
        return dto;
    }

    @Test
    void createRequest_shouldCreateRequest_whenDataValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor()));
        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        ItemRequestDto created = requestService.create(validRequestDto(), 1L);

        assertNotNull(created.getId());
        assertEquals("Нужна дрель", created.getDescription());
    }

    @Test
    void getOwnRequests_shouldReturnUserRequests() {
        ItemRequest request = validItemRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor()));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestIdOrderByIdAsc(anyLong())).thenReturn(new ArrayList<>());

        List<ItemRequestDto> requests = requestService.getOwnRequests(1L);

        assertEquals(1, requests.size());
        assertEquals("Нужна дрель", requests.get(0).getDescription());
    }

    @Test
    void getAllRequests_shouldReturnRequestsFromOtherUsers() {
        User otherUser = new User(2L, "bob@mail.com", "Bob");
        ItemRequest request = validItemRequest();

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(requestRepository.findAllOtherUsersRequests(eq(2L), any()))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestIdOrderByIdAsc(anyLong())).thenReturn(new ArrayList<>());

        List<ItemRequestDto> requests = requestService.getAllRequests(2L, 0, 20);

        assertEquals(1, requests.size());
    }

    @Test
    void getRequestById_shouldReturnRequest() {
        ItemRequest request = validItemRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor()));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestIdOrderByIdAsc(anyLong())).thenReturn(new ArrayList<>());

        ItemRequestDto found = requestService.getRequestById(1L, 1L);

        assertEquals(1L, found.getId());
        assertEquals("Нужна дрель", found.getDescription());
    }

    @Test
    void getRequestById_shouldIncludeItems_whenItemsExist() {
        ItemRequest request = validItemRequest();
        Item item = new Item();
        item.setId(100L);
        item.setName("Дрель");
        item.setDescription("Мощная");
        item.setAvailable(true);
        item.setOwner(requestor());

        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor()));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        List<Item> items = new ArrayList<>();
        items.add(item);
        when(itemRepository.findByRequestIdOrderByIdAsc(1L)).thenReturn(items);

        ItemRequestDto found = requestService.getRequestById(1L, 1L);

        assertNotNull(found.getItems());
        assertEquals(1, found.getItems().size());
        assertEquals(100L, found.getItems().get(0).getId());
    }

    @Test
    void createRequest_shouldThrowException_whenDescriptionEmpty() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("");

        assertThrows(IllegalArgumentException.class,
                () -> requestService.create(requestDto, 1L));
    }

    @Test
    void createRequest_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestService.create(validRequestDto(), 999L));
    }

    @Test
    void getRequestById_shouldThrowException_whenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor()));
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, 1L));
    }

    @Test
    void getRequestById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(1L, 999L));
    }
}