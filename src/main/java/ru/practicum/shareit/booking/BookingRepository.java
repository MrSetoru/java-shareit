package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerOrderByStartDesc(User booker);
    List<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker, LocalDateTime now, LocalDateTime now2);
    List<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime now);
    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime now);
    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    List<Booking> findByItemOwnerOrderByStartDesc(User owner);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = ?1 ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(User owner, LocalDateTime now, LocalDateTime now2);
    List<Booking> findByItemOwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime now);
    List<Booking> findByItemOwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime now);
    List<Booking> findByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status);

    List<Booking> findByItemAndStatusAndEndBeforeOrderByEndDesc(Item item, BookingStatus status, LocalDateTime end);

    List<Booking> findByItemAndStatusAndStartAfterOrderByStartAsc(Item item, BookingStatus status, LocalDateTime start);

    List<Booking> findByBookerAndItemAndEndBefore(User booker, Item item, LocalDateTime end);
}