package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(Long userId, Long itemId, BookingDto bookingDto);

    Booking approveBooking(Long userId, Long bookingId, Boolean approved);

    Booking getBooking(Long userId, Long bookingId);

    List<Booking> getBookingsByState(Long userId, String state);
}