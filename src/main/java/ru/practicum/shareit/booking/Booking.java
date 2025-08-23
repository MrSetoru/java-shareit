package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая бронирование вещи.
 */
@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с вещью (Item). Ленивая загрузка, так как детали вещи не всегда нужны.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Связь с пользователем, который бронирует (Booker). Ленивая загрузка.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    // Время начала бронирования.
    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    // Время окончания бронирования.
    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    // Статус бронирования. Хранится как строка (WAITING, APPROVED, REJECTED).
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    // --- Связь с ItemRequest (если ItemRequest будет отдельной сущностью) ---
    // Если ItemRequest не будет отдельной сущностью, а просто Long requestId в Item,
    // то это поле здесь не нужно, и оно будет в Item.
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "request_id")
    // private ItemRequest itemRequest;
}