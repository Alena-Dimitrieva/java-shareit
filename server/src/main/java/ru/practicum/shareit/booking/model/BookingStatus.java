package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    WAITING,    // новое бронирование, ожидает подтверждения
    APPROVED,   // бронирование подтверждено владельцем
    REJECTED,   // бронирование отклонено владельцем
    CANCELED    // бронирование отменено создателем
}
