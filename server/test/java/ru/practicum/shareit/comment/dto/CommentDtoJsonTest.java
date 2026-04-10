package ru.practicum.shareit.comment.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void serialize_shouldIncludeCreated() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 4, 10, 14, 0);
        CommentDto dto = new CommentDto(1L, "Отличная вещь", "Иван", now);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-04-10T14:00:00");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Иван");
    }

    @Test
    void deserialize_shouldIgnoreIdAndCreated() throws Exception {
        String content = "{\"text\":\"Комментарий\"}";

        CommentDto dto = json.parseObject(content);

        assertThat(dto.getText()).isEqualTo("Комментарий");
        assertThat(dto.getId()).isNull();
        assertThat(dto.getCreated()).isNull();
    }
}
