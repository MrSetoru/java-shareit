package ru.practicum.shareit.user;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.booking.Booking; // Импорт Booking
import ru.practicum.shareit.item.Item;     // Импорт Item

import java.util.List; // Импорт List

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true) // Email должен быть уникальным
    private String email;

    // Связь с вещами, которыми владеет пользователь. OneToMany, так как у пользователя много вещей.
    // mappedBy указывает на поле 'owner' в классе Item.
    // Ленивая загрузка.
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Item> items;

    // Связь с бронированиями, которые создал пользователь. OneToMany, так как пользователь может создать много бронирований.
    // mappedBy указывает на поле 'booker' в классе Booking.
    // Ленивая загрузка.
    @OneToMany(mappedBy = "booker", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    // ... (getter/setter, если не используешь Lombok @Data) ...
}