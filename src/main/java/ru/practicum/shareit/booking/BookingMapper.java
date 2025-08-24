package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

public interface BookingMapper {

    public abstract Booking toEntity(BookingDto bookingDto);

    public abstract BookingDto toDto(Booking booking);
}