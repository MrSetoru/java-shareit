package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingValidationException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @Valid @RequestBody BookingDto bookingDto) {
        try{
            return new ResponseEntity<>(bookingService.addBooking(userId, bookingDto), HttpStatus.CREATED);
        } catch (BookingValidationException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> confirmOrRejectBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @PathVariable Long bookingId,
                                                             @RequestParam Boolean approved) {
        return ResponseEntity.ok(bookingService.confirmOrRejectBooking(bookingId, userId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookingsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                   @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId, bookingState));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getAllBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                    @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(bookingService.getAllBookingsByOwnerId(userId, bookingState));
    }
}