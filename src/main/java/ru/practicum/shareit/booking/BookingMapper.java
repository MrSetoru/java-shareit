package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

public interface BookingMapper {

    // Абстрактный метод для преобразования BookingDto в Booking
    public abstract Booking toEntity(BookingDto bookingDto);

    // Абстрактный метод для преобразования Booking в BookingDto
    public abstract BookingDto toDto(Booking booking);
}