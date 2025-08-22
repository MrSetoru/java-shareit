package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query// 1. Поиск всех бронирований для пользователя (booker)
    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    @Query// 2. Поиск бронирований для пользователя (booker) с определенным статусом
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    // 3. Поиск бронирований для пользователя (booker), которые уже завершились (дата окончания в прошлом)
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.endTime < :now ORDER BY b.startTime DESC")
    List<Booking> findPastBookingsByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    // 4. Поиск бронирований для пользователя (booker), которые еще не начались (дата начала в будущем)
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.startTime > :now ORDER BY b.startTime DESC")
    List<Booking> findFutureBookingsByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    // 5. Поиск текущих бронирований для пользователя (booker) (дата начала в прошлом, дата окончания в будущем)
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.startTime <= :now AND b.endTime >= :now ORDER BY b.startTime DESC")
    List<Booking> findCurrentBookingsByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    // 6. Поиск всех бронирований для владельца вещи (owner)
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.startTime DESC")
    List<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // 7. Поиск бронирований для владельца вещи (owner) с определенным статусом
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status ORDER BY b.startTime DESC")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status, Pageable pageable);

    // 8. Поиск бронирований для владельца вещи (owner), которые уже завершились (дата окончания в прошлом)
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.endTime < :now ORDER BY b.startTime DESC")
    List<Booking> findPastBookingsByItemOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    // 9. Поиск бронирований для владельца вещи (owner), которые еще не начались (дата начала в будущем)
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.startTime > :now ORDER BY b.startTime DESC")
    List<Booking> findFutureBookingsByItemOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    // 10. Поиск текущих бронирований для владельца вещи (owner) (дата начала в прошлом, дата окончания в будущем)
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.startTime <= :now AND b.endTime >= :now ORDER BY b.startTime DESC")
    List<Booking> findCurrentBookingsByItemOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query// 11.  Проверка, что пользователь брал вещь в аренду (для добавления комментария)
    boolean existsBookingByItemIdAndBookerIdAndStatus(Long itemId, Long bookerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.startTime < :now ORDER BY b.startTime DESC")
    Booking findFirstByItem_IdAndStartTimeBeforeOrderByStartTimeDesc(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.startTime > :now ORDER BY b.startTime ASC")
    Booking findFirstByItem_IdAndStartTimeAfterOrderByStartTimeAsc(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.startTime < :startTime " +
            "ORDER BY b.startTime DESC")
    Booking findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(
            @Param("itemId") Long itemId,
            @Param("startTime") LocalDateTime startTime
    );

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.startTime > :now ORDER BY b.startTime ASC")
    Booking findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now
    );
}
