package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingDto; // Импорт BookingDto

/**
 * DTO для передачи информации о вещи вместе с ее последним и следующим бронированием.
 */
@Data
@Builder
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;

    // Последнее бронирование для этой вещи
    private BookingDto lastBooking;

    // Следующее бронирование для этой вещи
    private BookingDto nextBooking;
}