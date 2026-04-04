package ru.practicum.shareit.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение для ошибок валидации бронирования
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookingValidationException extends RuntimeException {
  public BookingValidationException(String message) {
    super(message);
  }
}