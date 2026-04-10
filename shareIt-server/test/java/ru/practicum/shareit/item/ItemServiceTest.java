package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private CommentRepository commentRepository;

    private ItemServiceImpl itemService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        itemService = new ItemServiceImpl(userRepository, itemRepository, bookingRepository,
                itemRequestRepository, commentRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private User owner() {
        return new User(1L, "alice@mail.com", "Alice");
    }

    private Item validItemEntity() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner());
        return item;
    }

    private ItemDto validItemDto() {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Мощная дрель");
        dto.setAvailable(true);
        return dto;
    }

    // Позитивные тесты
    @Test
    void createItem_shouldCreateItem_whenDataValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        ItemDto created = itemService.create(validItemDto(), 1L);

        assertNotNull(created.getId());
        assertEquals("Дрель", created.getName());
        assertTrue(created.getAvailable());
    }

    @Test
    void updateItem_shouldUpdateFields_whenOwnerUpdates() {
        Item item = validItemEntity();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новая дрель");
        updateDto.setDescription("Обновлённая");
        updateDto.setAvailable(false);

        ItemDto updated = itemService.update(1L, updateDto, 1L);

        assertEquals("Новая дрель", updated.getName());
        assertFalse(updated.getAvailable());
    }

    @Test
    void getItemById_shouldReturnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(validItemEntity()));

        ItemDto found = itemService.getById(1L, 1L);

        assertEquals(1L, found.getId());
    }

    @Test
    void getAllByOwner_shouldReturnOwnerItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(validItemEntity(), validItemEntity()));

        List<ItemDto> items = itemService.getAllByOwner(1L);

        assertEquals(2, items.size());
    }

    @Test
    void search_shouldReturnMatchingItems() {
        when(itemRepository.searchAvailableByText("дрель")).thenReturn(List.of(validItemEntity()));

        List<ItemDto> result = itemService.search("дрель");

        assertEquals(1, result.size());
    }

    // Негативные тесты
    @Test
    void createItem_shouldThrowException_whenAvailableNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));

        ItemDto item = new ItemDto();
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(null);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.create(item, 1L));
    }

    @Test
    void createItem_shouldThrowException_whenNameEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));

        ItemDto item = new ItemDto();
        item.setName("");
        item.setDescription("Описание");
        item.setAvailable(true);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.create(item, 1L));
    }

    @Test
    void updateItem_shouldThrowForbidden_whenNotOwner() {
        Item item = validItemEntity();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenOperationException.class,
                () -> itemService.update(1L, validItemDto(), 2L));
    }

    @Test
    void updateItem_shouldThrowException_whenItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.update(999L, validItemDto(), 1L));
    }

    @Test
    void getItemById_shouldThrowException_whenItemNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.getById(999L, 1L));
    }

    @Test
    void search_shouldReturnEmpty_whenTextBlank() {
        List<ItemDto> result = itemService.search("");
        assertTrue(result.isEmpty());
    }
}
