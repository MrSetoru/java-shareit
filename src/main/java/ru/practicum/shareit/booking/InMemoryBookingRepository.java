package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Optional<Booking> getBookingById(Long bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    @Override
    public List<Booking> getBookingsByBookerId(Long bookerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getBooker().getId().equals(bookerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getBookingsByItemId(Long itemId) {
        return bookings.values().stream()
                .filter(booking -> booking.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
    }

    @Override
    public Booking createBooking(Booking booking) {
        booking.setId(nextId++);
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Booking updateBooking(Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }
}