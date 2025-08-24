package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
@Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId")
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.status = :status")
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);
@Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);
@Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status")
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = :status")
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime start, LocalDateTime end);
@Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :end")
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);
@Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :end")
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start);
@Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :end AND b.end > :start")
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime start, LocalDateTime end);
@Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end > :start")
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end);
@Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :start")
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start);
}