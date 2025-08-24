package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingCreateDto;
import ru.practicum.shareit.booking.BookingDto;
//import ru.practicum.shareit.booking.BookingShortDto; // Если будем его использовать

import java.util.List;

public interface BookingService {


    BookingDto createBooking(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto approveOrRejectBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllBookingsByUser(Long userId, String state);

    List<BookingDto> getAllBookingsByOwner(Long userId, String state);
}