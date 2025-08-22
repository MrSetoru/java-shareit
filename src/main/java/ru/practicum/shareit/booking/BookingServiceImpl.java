package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingCreateDto bookingCreateDto, Long bookerId) {
        log.info("Creating booking for item {} by user {}", bookingCreateDto.getItemId(), bookerId);

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + bookerId + " not found."));

        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + bookingCreateDto.getItemId() + " not found."));

        if (item.getOwner() == null) {
            log.error("Item owner is null for item id: {}", item.getId());
            throw new IllegalStateException("Item owner is null!");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            log.warn("User with id {} tried to book their own item with id {}", bookerId, item.getId());
            throw new ValidationException("User cannot book their own item.");
        }

        if (!item.getAvailable()) {
            log.warn("Item with id {} is not available for booking.", item.getId());
            throw new ValidationException("Item is not available.");
        }

        if (bookingCreateDto.getStartTime() == null || bookingCreateDto.getEndTime() == null || bookingCreateDto.getStartTime().isAfter(bookingCreateDto.getEndTime()) || bookingCreateDto.getStartTime().isEqual(bookingCreateDto.getEndTime())) {
            log.warn("Invalid booking dates: startTime={}, endTime={}", bookingCreateDto.getStartTime(), bookingCreateDto.getEndTime());
            throw new ValidationException("Invalid booking dates.");
        }

        Booking booking = Booking.builder()
                .booker(booker)
                .item(item)
                .startTime(bookingCreateDto.getStartTime())
                .endTime(bookingCreateDto.getEndTime())
                .status(BookingStatus.WAITING)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        log.info("Approving booking {} by owner {} with approved={}", bookingId, ownerId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with id " + bookingId + " not found."));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("User with id {} tried to approve/reject booking with id {} which they do not own.", ownerId, bookingId);
            throw new AccessDeniedException("User is not the owner of the item for this booking.");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Booking with id {} is already in status {} and cannot be changed.", bookingId, booking.getStatus());
            throw new ConflictException("Booking is not in WAITING state.");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
            log.info("Booking with id {} approved.", bookingId);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.info("Booking with id {} rejected.", bookingId);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long bookingId, Long userId) {
        log.info("Getting booking {} for user {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with id " + bookingId + " not found."));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("User with id {} tried to access booking with id {} which they are not related to.", userId, bookingId);
            throw new AccessDeniedException("User cannot access this booking.");
        }

        return booking;
    }

    @Override
    public List<Booking> getBookingsByBookerId(Long bookerId, String state, Pageable pageable) {
        log.info("Getting bookings for booker {} with state {}", bookerId, state);

        userRepository.findById(bookerId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + bookerId + " not found."));

        LocalDateTime now = LocalDateTime.now();
        BookingStatus bookingStatus = BookingStatus.from(state).orElse(null);

        List<Booking> bookings;

        switch (state) {
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsByBookerId(bookerId, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsByBookerId(bookerId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsByBookerId(bookerId, now, pageable);
                break;
            case "WAITING":
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(bookerId, bookingStatus, pageable);
                break;
            default:
                bookings = bookingRepository.findByBookerId(bookerId, pageable);
                break;
        }

        return bookings;
    }

    @Override
    public List<Booking> getBookingsByOwnerId(Long ownerId, String state, Pageable pageable) {
        log.info("Getting bookings for owner {} with state {}", ownerId, state);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + ownerId + " not found."));

        LocalDateTime now = LocalDateTime.now();
        BookingStatus bookingStatus = BookingStatus.from(state).orElse(null);

        List<Booking> bookings;

        switch (state) {
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsByItemOwnerId(ownerId, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsByItemOwnerId(ownerId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsByItemOwnerId(ownerId, now, pageable);
                break;
            case "WAITING":
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, bookingStatus, pageable);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerId(ownerId, pageable);
                break;
        }

        return bookings;
    }

    public boolean hasUserBookedItem(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.existsBookingByItemIdAndBookerIdAndStatus(itemId, userId, BookingStatus.APPROVED);
    }
}