package ru.practicum.shareit.booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Optional<Booking> getBookingById(Long bookingId);

    List<Booking> getBookingsByBookerId(Long bookerId);

    List<Booking> getBookingsByItemId(Long itemId);

    Booking createBooking(Booking booking);

    Booking updateBooking(Booking booking);
}
