package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        log.info("Received request to add booking for userId: {} with bookingDto: {}", userId, bookingDto);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + bookingDto.getItemId() + " not found"));

        if (!item.getAvailable()) {
            log.warn("Attempt to book unavailable item with id: {}", item.getId());
            throw new ValidationException("Item is not available for booking");
        }

        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start == null) {
            log.warn("Booking creation failed: start date is null for userId: {}, itemId: {}", userId, item.getId());
            throw new BookingValidationException("Start date cannot be null");
        }
        if (end == null) {
            log.warn("Booking creation failed: end date is null for userId: {}, itemId: {}", userId, item.getId());
            throw new BookingValidationException("End date cannot be null");
        }

        if (start.isBefore(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
            log.warn("Booking creation failed: start or end date is in the past for userId: {}, itemId: {}", userId, item.getId());
            throw new BookingValidationException("Start and end dates must be in the future");
        }

        if (start.isAfter(end)) {
            log.warn("Booking creation failed: start date '{}' is after end date '{}' for userId: {}, itemId: {}", start, end, userId, item.getId());
            throw new BookingValidationException("Start date cannot be after end date");
        }

        if (start.equals(end)) {
            log.warn("Booking creation failed: start date is equal to end date for userId: {}, itemId: {}", userId, item.getId());
            throw new BookingValidationException("Start date cannot be the same as end date");
        }

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Booking creation failed: owner (userId: {}) tried to book their own item (itemId: {})", userId, item.getId());
            throw new ForbiddenException("Owner cannot book their own item");
        }

        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(start);
        booking.setEnd(end);

        booking = bookingRepository.save(booking);
        log.info("Booking created successfully for userId: {}, bookingId: {}", userId, booking.getId());

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto confirmOrRejectBooking(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the item owner can confirm or reject the booking");
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
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the booker or the item owner can view the booking");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

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
            case CANCELED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.CANCELED, sort);
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
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

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
            case CANCELED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.CANCELED, sort);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}