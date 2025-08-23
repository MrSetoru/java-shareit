package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.*; // Импорт всех нужных исключений
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
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper; // Маппер для бронирований

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingCreateDto bookingCreateDto) {
        // 1. Проверки: пользователь, вещь
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + bookingCreateDto.getItemId() + " not found"));

        // 2. Проверка доступности вещи:
        //    - Нельзя бронировать свою вещь
        //    - Вещь должна быть доступна (available = true)
        //    - Временной интервал не должен пересекаться с существующими бронированиями
        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User cannot book their own item");
        }
        if (!item.getAvailable()) {
            throw new ItemNotFoundException("Item with id " + item.getId() + " is not available");
        }
        // Проверка на пересечение дат
        validateBookingDates(bookingCreateDto.getStart(), bookingCreateDto.getEnd());
        if (!bookingRepository.findOverlappingBookingsForIte(item.getId(), bookingCreateDto.getStart(), bookingCreateDto.getEnd()).isEmpty()) {
            throw new ItemNotFoundException("Item with id " + item.getId() + " is already booked for the requested period");
        }

        // 3. Создание сущности Booking
        Booking booking = bookingMapper.toBooking(bookingCreateDto);
        booking.setBooker(booker); // Устанавливаем бронирующего
        booking.setItem(item);     // Устанавливаем вещь
        booking.setStatus(BookingStatus.WAITING); // Статус по умолчанию

        // 4. Сохранение и возврат DTO
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveOrRejectBooking(Long userId, Long bookingId, boolean approved) {
        // 1. Проверки: пользователь, бронирование
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with id " + bookingId + " not found"));

        // 2. Проверка прав: только владелец вещи может подтверждать/отклонять
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not the owner of the item, cannot approve/reject booking");
        }

        // 3. Проверка текущего статуса: нельзя менять статус, если он уже не WAITING
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking status is already " + booking.getStatus() + ", cannot change.");
        }

        // 4. Обновление статуса
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        // 5. Сохранение и возврат DTO
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long userId, Long bookingId) {
        // 1. Проверки: пользователь, бронирование
        // Используем кастомный метод репозитория, который проверяет, что пользователь либо автор, либо владелец
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with id " + bookingId + " not found for user " + userId));

        // 2. Возврат DTO
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookingsByUser(Long userId, String state, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        // Убираем парсинг в enum, обрабатываем строки напрямую
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        // Для всех запросов, где state может быть null или "ALL", нам нужны все бронирования (кроме WAITING/REJECTED, если это не явно запрошено)
        // Или же просто все бронирования, а потом фильтровать.
        // Лучше использовать репозиторий и его возможности, или явные запросы.

        // Давайте сделаем switch по строке state
        switch (state) {
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            case "CURRENT":
                bookings = bookingRepository.findBookerCurrentBookings(userId, now);
                break;
            case "PAST":
                bookings = bookingRepository.findBookerPastBookings(userId, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findBookerFutureBookings(userId, now);
                break;
            case "APPROVED": // Добавляем APPROVED как отдельный статус
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.APPROVED);
                break;
            case "ALL": // Если state = ALL, возвращаем все, кроме WAITING и REJECTED
            case "all": // Обрабатываем оба регистра
            default: // Если state null, пустой или неизвестный, по ТЗ возвращаем ALL
                // Здесь нужно вернуть все, кроме WAITING и REJECTED.
                // Это можно сделать, например, получив все бронирования и отфильтровав.
                // Или, что лучше, используя более сложный JPQL запрос, который SELECT FROM Booking b WHERE b.booker.id = :userId AND b.status NOT IN ('WAITING', 'REJECTED') ORDER BY b.start DESC
                // Для простоты, давайте получим все и отфильтруем.
                List<Booking> allUserBookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                bookings = allUserBookings.stream()
                        .filter(b -> b.getStatus() != BookingStatus.WAITING && b.getStatus() != BookingStatus.REJECTED)
                        .collect(Collectors.toList());
                break;
        }
        // Если state == null, он попадет в default, что приведет к ALL. Это соответствует требованию.

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookingsByOwner(Long userId, String state, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            case "CURRENT":
                bookings = bookingRepository.findOwnerCurrentBookings(userId, now);
                break;
            case "PAST":
                bookings = bookingRepository.findOwnerPastBookings(userId, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findOwnerFutureBookings(userId, now);
                break;
            case "APPROVED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.APPROVED);
                break;
            case "ALL":
            case "all":
            default:
                // Возвращаем все, кроме WAITING и REJECTED
                List<Booking> allOwnerBookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
                bookings = allOwnerBookings.stream()
                        .filter(b -> b.getStatus() != BookingStatus.WAITING && b.getStatus() != BookingStatus.REJECTED)
                        .collect(Collectors.toList());
                break;
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    /**
     * Вспомогательный метод для валидации дат начала и конца бронирования.
     * @param start Дата начала.
     * @param end Дата окончания.
     * @throws ValidationException если даты некорректны.
     */
    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || start.isEqual(end) || start.isAfter(end)) {
            throw new ValidationException("Invalid booking dates: start must be before end.");
        }
    }

    /**
     * Вспомогательный метод для парсинга строки состояния в BookingStatus enum.
     * @param state Строка состояния.
     * @return BookingStatus enum.
     * @throws ValidationException если строка состояния некорректна.
     */
//    private BookingStatus parseBookingStatus(String state) {
//        if (state == null || state.isBlank() || state.equalsIgnoreCase("ALL")) {
//            return BookingStatus.ALL; // По умолчанию или если "ALL"
//        }
//        try {
//            // Приводим к верхнему регистру для сравнения с enum
//            return BookingStatus.valueOf(state.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new ValidationException("Unknown state: " + state);
//        }
//    }
}