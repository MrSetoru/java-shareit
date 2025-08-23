package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingCreateDto;
import ru.practicum.shareit.booking.BookingDto;
//import ru.practicum.shareit.booking.BookingShortDto; // Если будем его использовать
import ru.practicum.shareit.booking.BookingStatus; // Для фильтрации

import java.util.List;

public interface BookingService {


    BookingDto createBooking(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto approveOrRejectBooking(Long userId, Long bookingId, boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllBookingsByUser(Long userId, String state, Pageable pageable);

    List<BookingDto> getAllBookingsByOwner(Long userId, String state, Pageable pageable);
}