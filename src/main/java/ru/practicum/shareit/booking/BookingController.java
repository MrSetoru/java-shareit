package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestParam("itemId") Long itemId,
                                    @RequestBody BookingDto bookingDto) {
        return BookingMapper.toBookingDto(bookingService.createBooking(userId, itemId, bookingDto));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam("approved") Boolean approved) {
        return BookingMapper.toBookingDto(bookingService.approveBooking(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long bookingId) {
        return BookingMapper.toBookingDto(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public List<BookingDto> getBookingsByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(value = "state", defaultValue = "ALL") String state) {
        return bookingService.getBookingsByState(userId, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}