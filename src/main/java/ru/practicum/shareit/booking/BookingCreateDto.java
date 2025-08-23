package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для создания нового бронирования.
 * bookerId берется из заголовка X-Sharer-User-Id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {

    // ID вещи, которую хотят забронировать.
    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    // Время начала бронирования. Должно быть в настоящем или будущем.
    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime start;

    // Время окончания бронирования. Должно быть после времени начала.
    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    private LocalDateTime end;
}