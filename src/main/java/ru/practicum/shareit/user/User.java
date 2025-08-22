package ru.practicum.shareit.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email формат")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
}