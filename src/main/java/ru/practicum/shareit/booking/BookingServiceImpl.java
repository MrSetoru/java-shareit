package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + bookingDto.getItemId() + " not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start == null || end == null) {
            throw new BookingValidationException("Start and end dates cannot be null");
        }

        if (!start.isBefore(end)) { // start >= end
            throw new BookingValidationException("Start date must be before end date");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Owner cannot book their own item");
        }

        Booking booking = bookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);

        UserDto bookerDto = userMapper.toUserDto(booker);
        ItemDto itemDto = itemMapper.toItemDto(item);

        BookingDto bookingDtoResult = bookingMapper.toBookingDto(booking, itemDto, bookerDto);
        bookingDtoResult.setStatus(BookingStatus.WAITING.toString());

        return bookingDtoResult;
    }

    @Override
    @Transactional
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

        Item item = booking.getItem();
        User booker = booking.getBooker();
        ItemDto itemDto = itemMapper.toItemDto(item);
        UserDto bookerDto = userMapper.toUserDto(booker);
        BookingDto bookingDtoResult = bookingMapper.toBookingDto(booking, itemDto, bookerDto);
        bookingDtoResult.setStatus(booking.getStatus().toString());
        return bookingDtoResult;
    }

    @Override
    @Transactional
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the booker or the item owner can view the booking");
        } else {
            Item item = booking.getItem();
            User booker = booking.getBooker();
            ItemDto itemDto = itemMapper.toItemDto(item);
            UserDto bookerDto = userMapper.toUserDto(booker);

            BookingDto bookingDtoResult = bookingMapper.toBookingDto(booking, itemDto, bookerDto);
            bookingDtoResult.setStatus(booking.getStatus().toString());
            return bookingDtoResult;
        }
    }

    @Override
    @Transactional
    public List<BookingDto> getAllBookingsByUserId(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
                .map(booking -> {
                    Item item = booking.getItem();
                    User booker = booking.getBooker();
                    ItemDto itemDto = itemMapper.toItemDto(item);
                    UserDto bookerDto = userMapper.toUserDto(booker);
                    BookingDto bookingDtoResult = bookingMapper.toBookingDto(booking, itemDto, bookerDto);
                    bookingDtoResult.setStatus(booking.getStatus().toString());
                    return bookingDtoResult;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BookingDto> getAllBookingsByOwnerId(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
                .map(booking -> {
                    Item item = booking.getItem();
                    User booker = booking.getBooker();
                    ItemDto itemDto = itemMapper.toItemDto(item);
                    UserDto bookerDto = userMapper.toUserDto(booker);
                    BookingDto bookingDtoResult = bookingMapper.toBookingDto(booking, itemDto, bookerDto);
                    bookingDtoResult.setStatus(booking.getStatus().toString());
                    return bookingDtoResult;
                }).collect(Collectors.toList());
    }
}