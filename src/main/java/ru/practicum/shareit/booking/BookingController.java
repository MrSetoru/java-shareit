package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validation.ValueOfEnum;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated // Enable validation for path variables and request parameters
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestHeader(USER_ID_HEADER) Long bookerId,
            @RequestBody @Valid BookingCreateDto bookingCreateDto) {

        log.info("Received POST request to create booking for item {} by user {}", bookingCreateDto.getItemId(), bookerId);
        Booking booking = bookingService.createBooking(bookingCreateDto, bookerId);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Booking> approveBooking(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {

        log.info("Received PATCH request to approve booking {} by owner {} with approved={}", bookingId, ownerId, approved);
        Booking booking = bookingService.approveBooking(ownerId, bookingId, approved);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBooking(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long bookingId) {

        log.info("Received GET request to get booking {} for user {}", bookingId, userId);
        Booking booking = bookingService.getBooking(bookingId, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getBookingsByBookerId(
            @RequestHeader(USER_ID_HEADER) Long bookerId,
            @RequestParam(defaultValue = "ALL") @ValueOfEnum(enumClass = BookingState.class) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Received GET request to get bookings for booker {} with state {}", bookerId, state);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("startTime").descending());
        List<Booking> bookings = bookingService.getBookingsByBookerId(bookerId, state, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<Booking>> getBookingsByOwnerId(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") @ValueOfEnum(enumClass = BookingState.class) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Received GET request to get bookings for owner {} with state {}", ownerId, state);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("startTime").descending());
        List<Booking> bookings = bookingService.getBookingsByOwnerId(ownerId, state, pageable);
        return ResponseEntity.ok(bookings);
    }
}