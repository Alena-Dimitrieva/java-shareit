package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//Проверяет создание запросов и получение с вещами
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createRequestAndGetWithItems() {
        UserDto requestor = userService.create(new UserDto(null, "req@test.com", "Requestor"));
        UserDto owner = userService.create(new UserDto(null, "owner@test.com", "Owner"));
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestDto savedRequest = requestService.create(requestDto, requestor.getId());
        ItemDto item = new ItemDto();
        item.setName("Дрель");
        item.setDescription("Мощная");
        item.setAvailable(true);
        item.setRequestId(savedRequest.getId());
        itemService.create(item, owner.getId());

        ItemRequestDto found = requestService.getRequestById(savedRequest.getId(), owner.getId());

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void getAllRequests_excludeOwn() {
        UserDto user1 = userService.create(new UserDto(null, "u1@test.com", "U1"));
        UserDto user2 = userService.create(new UserDto(null, "u2@test.com", "U2"));
        ItemRequestDto req1 = new ItemRequestDto();
        req1.setDescription("Запрос 1");
        requestService.create(req1, user1.getId());

        ItemRequestDto req2 = new ItemRequestDto();
        req2.setDescription("Запрос 2");
        requestService.create(req2, user2.getId());

        List<ItemRequestDto> all = requestService.getAllRequests(user1.getId(), 0, 20);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getDescription()).isEqualTo("Запрос 2");
    }
}
