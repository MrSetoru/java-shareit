package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingService;
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
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start.isAfter(end)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        if (start.equals(end)) {
            throw new ValidationException("Start date cannot be the same as end date");
        }

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

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(user, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerAndStartAfterOrderByStartDesc(user, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED);
                break;
            case ALL:
                bookings = bookingRepository.findByBookerOrderByStartDesc(user);
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

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(user, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerAndEndBeforeOrderByStartDesc(user, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerAndStartAfterOrderByStartDesc(user, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED);
                break;
            case ALL:
                bookings = bookingRepository.findByItemOwnerOrderByStartDesc(user);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}