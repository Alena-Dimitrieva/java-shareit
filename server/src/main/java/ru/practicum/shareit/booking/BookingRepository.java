package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(Long bookerId);

    List<Booking> findByItem_IdAndBooker_Id(Long itemId, Long bookerId);

    Optional<Booking> findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(
            Long itemId, BookingStatus status, LocalDateTime end);

    Optional<Booking> findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime start);

    List<Booking> findByBooker_IdOrderByStartDesc(Long userId);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(
            Long userId, LocalDateTime end);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(
            Long userId, LocalDateTime start);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(
            Long userId, BookingStatus status);

    List<Booking> findByItem_Owner_IdOrderByStartDesc(Long ownerId);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end);

    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start);

    List<Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status);
}
