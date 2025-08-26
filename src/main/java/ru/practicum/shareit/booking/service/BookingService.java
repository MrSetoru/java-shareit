package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(Long userId, BookingDto bookingDto);

    BookingDto confirmOrRejectBooking(Long bookingId, Long userId, Boolean approved);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getAllBookingsByUserId(Long userId, BookingState state);

    List<BookingDto> getAllBookingsByOwnerId(Long userId, BookingState state);
}