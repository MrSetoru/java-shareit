package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.InMemoryItemRepository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.InMemoryUserRepository;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final InMemoryBookingRepository bookingRepository;
    private final InMemoryItemRepository itemRepository;
    private final InMemoryUserRepository userRepository;

    @Override
    public Booking createBooking(Long userId, Long itemId, BookingDto bookingDto) {
        Item item = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        User booker = userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!item.getAvailable()) {
            throw new RuntimeException("Item is not available for booking");
        }

        Booking booking = BookingMapper.fromBookingDto(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.createBooking(booking);
    }

    @Override
    public Booking approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.getBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new RuntimeException("User is not the owner of the item");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingRepository.updateBooking(booking);
    }

    @Override
    public Booking getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.getBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new RuntimeException("User is not the owner or booker of the item");
        }
        return booking;
    }

    @Override
    public List<Booking> getBookingsByState(Long userId, String state) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.getBookingsByBookerId(userId);

        BookingState bookingState = BookingState.valueOf(state.toUpperCase());

        switch (bookingState) {
            case ALL:
                break;
            case CURRENT:
                bookings = bookings.stream().filter(booking -> booking.getStart().isBefore(java.time.LocalDateTime.now())
                        && booking.getEnd().isAfter(java.time.LocalDateTime.now())).collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookings.stream().filter(booking -> booking.getEnd().isBefore(java.time.LocalDateTime.now())).collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookings.stream().filter(booking -> booking.getStart().isAfter(java.time.LocalDateTime.now())).collect(Collectors.toList());
                break;
            case WAITING:
                bookings = bookings.stream().filter(booking -> booking.getStatus().equals(BookingStatus.WAITING)).collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = bookings.stream().filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED)).collect(Collectors.toList());
                break;
        }

        return bookings;
    }

    private enum BookingState {
        ALL,
        CURRENT,
        PAST,
        FUTURE,
        WAITING,
        REJECTED
    }
}