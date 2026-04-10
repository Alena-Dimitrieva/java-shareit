package ru.practicum.shareit.exception;

/**
 * Исключение для ошибок валидации бронирования
 */
public class BookingValidationException extends RuntimeException {
  public BookingValidationException(String message) {
    super(message);
  }
}