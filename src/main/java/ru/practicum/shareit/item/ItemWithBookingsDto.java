package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingDto;

@Data
@Builder
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long lastBookingId;
    private Long nextBookingId;
    private Long requestId;
    private Long ownerId;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
}