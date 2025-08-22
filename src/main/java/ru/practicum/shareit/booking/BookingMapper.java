package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartTime()) // Use getStartTime
                .end(booking.getEndTime())   // Use getEndTime
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .build();
    }

    public static Booking fromBookingDto(BookingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId())
                .startTime(bookingDto.getStart()) // Use getStart from DTO
                .endTime(bookingDto.getEnd())   // Use getEnd from DTO
                .item(item)
                .booker(booker)
                .status(bookingDto.getStatus())
                .build();
    }
}