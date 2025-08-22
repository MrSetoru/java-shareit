package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingCreateDto bookingCreateDto, Long bookerId);

    Booking approveBooking(Long ownerId, Long bookingId, Boolean approved);

    Booking getBooking(Long bookingId, Long userId);

    List<Booking> getBookingsByBookerId(Long bookerId, String state, Pageable pageable);

    List<Booking> getBookingsByOwnerId(Long ownerId, String state, Pageable pageable);
}