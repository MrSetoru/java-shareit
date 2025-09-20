package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        // Проверка на null для itemId и bookerId
        if (bookingDto.getItemId() == null) {
            throw new BookingValidationException("Item ID cannot be null");
        }
        if (bookingDto.getBookerId() == null) {
            throw new BookingValidationException("Booker ID cannot be null");
        }

        // Получение User и Item из репозитория
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        // Получение start и end из BookingDto
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        // Проверка на null для start и end
        if (start == null) {
            throw new BookingValidationException("Start date cannot be null");
        }
        if (end == null) {
            throw new BookingValidationException("End date cannot be null");
        }

        // Проверка дат
        if (start.isBefore(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
            throw new BookingValidationException("Start and end dates must be in the future");
        }
        if (start.isAfter(end)) {
            throw new BookingValidationException("Start date cannot be after end date");
        }
        if (start.equals(end)) {
            throw new BookingValidationException("Start date cannot be the same as end date");
        }

        // Проверка, что владелец не может забронировать свою вещь
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book their own item");
        }

        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto confirmOrRejectBooking(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only the item owner can confirm or reject the booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Cannot confirm or reject a booking that is not in WAITING status");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only the booker or the item owner can view the booking");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, BookingState state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
                break;
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, sort);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsByOwnerId(Long userId, BookingState state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
                break;
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(userId, sort);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}