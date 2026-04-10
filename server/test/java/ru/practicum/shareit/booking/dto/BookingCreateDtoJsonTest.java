package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

//Тест для проверки сериализации/десериализации и валидации
@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void serialize_shouldContainDatesInISOFormat() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 12, 15, 30);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(start);
        dto.setEnd(end);

        JsonContent<BookingCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-04-10T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-04-12T15:30:00");
    }

    @Test
    void deserialize_shouldParseISOFormat() throws Exception {
        String content = "{\"itemId\":2,\"start\":\"2026-05-01T10:00:00\",\"end\":\"2026-05-05T10:00:00\"}";

        BookingCreateDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(2L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 5, 5, 10, 0));
    }
}
