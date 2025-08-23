package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- Получение конкретного бронирования ---
    // Найти бронирование по ID и убедиться, что запрашивает либо автор, либо владелец вещи
    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.id = :bookingId AND (b.booker.id = :userId OR b.item.owner.id = :userId)")
    Optional<Booking> findByIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

    // --- Получение бронирований по автору (booker) ---
    // Метод для получения всех бронирований автора (сортировка по убыванию времени начала)
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    // Метод для получения бронирований автора по статусу
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Методы для фильтрации по времени для автора
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'APPROVED' AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findBookerCurrentBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'APPROVED' AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findBookerPastBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findBookerFutureBookings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // --- Получение бронирований для владельца вещей (item.owner) ---
    // Метод для получения всех бронирований для вещей владельца (сортировка по убыванию времени начала)
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    // Метод для получения бронирований для вещей владельца по статусу
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    // Методы для фильтрации по времени для владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'APPROVED' AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findOwnerCurrentBookings(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'APPROVED' AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findOwnerPastBookings(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findOwnerFutureBookings(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // --- Вспомогательные методы для проверки доступности вещи ---
    // Найти бронирования для конкретной вещи, которые пересекаются с указанным временным интервалом.
    // Используется для проверки, свободна ли вещь.
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND (b.start < :end AND b.end > :start)")
    List<Booking> findOverlappingBookingsForIte(@Param("itemId") Long itemId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // --- Методы для получения последнего/следующего бронирования (для ItemWithBookingsDto) ---
    // Находит первое бронирование для вещи, которое закончилось ДО текущего момента (последнее).
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start < :now ORDER BY b.start DESC")
    List<Booking> findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Находит первое бронирование для вещи, которое начнется ПОСЛЕ текущего момента (следующее).
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}