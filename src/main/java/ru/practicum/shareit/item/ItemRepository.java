package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.bookings " +
            "LEFT JOIN FETCH i.comments " +
            "WHERE upper(i.name) LIKE upper(concat('%', :text, '%')) OR upper(i.description) LIKE upper(concat('%', :text, '%'))")
    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("text") String text, @Param("text") String text1);

    List<Item> findByAvailableTrueAndOwnerId(Long ownerId);

    List<Item> findByRequestId(Long requestId);

    Optional<Booking> findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(Long itemId, BookingStatus status, LocalDateTime end);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, BookingStatus status, LocalDateTime start);
}