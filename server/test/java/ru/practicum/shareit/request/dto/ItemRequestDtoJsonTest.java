package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void deserialize_shouldOnlyRequireDescription() throws Exception {
        String content = "{\"description\":\"Нужен перфоратор\"}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getDescription()).isEqualTo("Нужен перфоратор");
        assertThat(dto.getId()).isNull();
        assertThat(dto.getCreated()).isNull();
    }

    @Test
    void serialize_shouldIncludeCreated() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 4, 10, 9, 0);
        ItemRequestDto dto = new ItemRequestDto(1L, "Запрос", 1L, created, null);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-04-10T09:00:00");
    }
}