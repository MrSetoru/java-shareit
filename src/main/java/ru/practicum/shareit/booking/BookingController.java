package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Импорт Sort
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // --- POST /bookings ---
    // Добавление нового запроса на бронирование
    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestBody BookingCreateDto bookingCreateDto) {
        // Валидация входных данных происходит в BookingCreateDto и BookingServiceImpl
        // userId берется из заголовка
        BookingDto createdBooking = bookingService.createBooking(userId, bookingCreateDto);
        // Статус 201 Created для успешного создания
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    // --- PATCH /bookings/{bookingId} ---
    // Подтверждение или отклонение запроса на бронирование
    @PatchMapping("/{bookingId}")
    public ResponseEntity<String> approveOrRejectBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @PathVariable Long bookingId,
                                                         @RequestParam Boolean approved) {
        // Parameter 'approved' must be 'true' or 'false'
        if (approved == null) {
            // Можно бросить исключение или вернуть BAD_REQUEST, если approved не указан
            // Например: throw new ValidationException("Parameter 'approved' cannot be null");
            // Или:
            return ResponseEntity.badRequest().body("Parameter 'approved' must be true or false.");
        }
        BookingDto updatedBooking = bookingService.approveOrRejectBooking(userId, bookingId, approved);
        return ResponseEntity.ok(String.valueOf(updatedBooking));
    }

    // --- GET /bookings/{bookingId} ---
    // Получение данных о конкретном бронировании
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long bookingId) {
        // userId - это тот, кто запрашивает (автор или владелец)
        BookingDto booking = bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(booking);
    }

    // --- GET /bookings?state={state} ---
    // Получение списка всех бронирований текущего пользователя (автора)
    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookingsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state, // По умолчанию "ALL"
            @RequestParam(defaultValue = "0") int from, // Параметр пагинации (отступ)
            @RequestParam(defaultValue = "10") int size  // Параметр пагинации (размер страницы)
    ) {
        // Создаем Pageable с сортировкой по дате от более новых к более старым (DESC)
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<BookingDto> bookings = bookingService.getAllBookingsByUser(userId, state, pageable);
        return ResponseEntity.ok(bookings);
    }

    // --- GET /bookings/owner?state={state} ---
    // Получение списка бронирований для всех вещей текущего пользователя (владельца)
    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getAllBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId, // ID владельца вещей
            @RequestParam(required = false, defaultValue = "ALL") String state, // По умолчанию "ALL"
            @RequestParam(defaultValue = "0") int from, // Параметр пагинации (отступ)
            @RequestParam(defaultValue = "10") int size  // Параметр пагинации (размер страницы)
    ) {
        // Создаем Pageable с сортировкой по дате от более новых к более старым (DESC)
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(userId, state, pageable);
        return ResponseEntity.ok(bookings);
    }
}