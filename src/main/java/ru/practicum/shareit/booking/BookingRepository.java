package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime now, LocalDateTime now1, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item JOIN FETCH b.booker WHERE b.id = :bookingId")
    Optional<Booking> findById(@Param("bookingId") Long bookingId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime now, LocalDateTime now1, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    Optional<Booking> findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(Long itemId, BookingStatus status, LocalDateTime end);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, BookingStatus status, LocalDateTime start);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus approved, Sort sort);
}