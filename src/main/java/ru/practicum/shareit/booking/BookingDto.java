package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus; // Импорт BookingStatus

import java.time.LocalDateTime;

/**
 * DTO для представления основных данных бронирования.
 */
@Data
@Builder
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;

    // ID вещи, которую бронируют
    private Long itemId;

    // ID пользователя, который бронирует
    private Long bookerId;
}