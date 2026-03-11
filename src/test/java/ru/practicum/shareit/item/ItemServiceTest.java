package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.Exception.ForbiddenOperationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private UserService userService;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemServiceImpl(userService);
    }

    private UserDto owner() {
        return new UserDto(1L, "Alice", "alice@mail.com");
    }

    private ItemDto validItem() {
        return new ItemDto(null, "Drill", "Power drill", true, null);
    }

    //Тесты с позитивным сценарием
    @Test
    void createItem_shouldCreateItem_whenDataValid() {

        when(userService.getUserById(1L)).thenReturn(owner());

        ItemDto created = itemService.create(validItem(), 1L);

        assertNotNull(created.getId());
        assertEquals("Drill", created.getName());
        assertTrue(created.getAvailable());
    }

    @Test
    void updateItem_shouldUpdateFields_whenOwnerUpdates() {

        when(userService.getUserById(1L)).thenReturn(owner());

        ItemDto created = itemService.create(validItem(), 1L);

        ItemDto updateDto = new ItemDto(null, "New Drill", "Updated", false, null);

        ItemDto updated = itemService.update(created.getId(), updateDto, 1L);

        assertEquals("New Drill", updated.getName());
        assertFalse(updated.getAvailable());
    }

    @Test
    void getItemById_shouldReturnItem() {

        when(userService.getUserById(1L)).thenReturn(owner());

        ItemDto created = itemService.create(validItem(), 1L);

        ItemDto found = itemService.getById(created.getId(), 1L);

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getAllByOwner_shouldReturnOwnerItems() {

        when(userService.getUserById(1L)).thenReturn(owner());

        itemService.create(validItem(), 1L);
        itemService.create(validItem(), 1L);

        List<ItemDto> items = itemService.getAllByOwner(1L);

        assertEquals(2, items.size());
    }

    @Test
    void search_shouldReturnMatchingItems() {

        when(userService.getUserById(1L)).thenReturn(owner());

        itemService.create(validItem(), 1L);

        List<ItemDto> result = itemService.search("drill");

        assertEquals(1, result.size());
    }

    //Тесты с негативным сценарием
    @Test
    void createItem_shouldThrowException_whenAvailableNull() {

        when(userService.getUserById(1L)).thenReturn(owner());

        ItemDto item = new ItemDto(null, "Drill", "Desc", null, null);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.create(item, 1L));
    }

    @Test
    void createItem_shouldThrowException_whenNameEmpty() {

        when(userService.getUserById(1L)).thenReturn(owner());

        ItemDto item = new ItemDto(null, "", "Desc", true, null);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.create(item, 1L));
    }

    @Test
    void updateItem_shouldThrowForbidden_whenNotOwner() {

        when(userService.getUserById(1L)).thenReturn(owner());

        ItemDto created = itemService.create(validItem(), 1L);

        assertThrows(ForbiddenOperationException.class,
                () -> itemService.update(created.getId(), validItem(), 2L));
    }

    @Test
    void updateItem_shouldThrowException_whenItemNotFound() {

        assertThrows(NoSuchElementException.class,
                () -> itemService.update(999L, validItem(), 1L));
    }

    @Test
    void getItemById_shouldThrowException_whenItemNotFound() {

        when(userService.getUserById(1L)).thenReturn(owner());

        assertThrows(NoSuchElementException.class,
                () -> itemService.getById(999L, 1L));
    }

    @Test
    void search_shouldReturnEmpty_whenTextBlank() {

        List<ItemDto> result = itemService.search("");

        assertTrue(result.isEmpty());
    }
}
