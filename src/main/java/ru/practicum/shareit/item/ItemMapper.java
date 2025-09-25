package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {

    private final BookingMapper bookingMapper;

    public ItemMapper(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    public ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequestId())
                .lastBooking(mapBookingToShortDto(getLastBooking(item.getBookings())))
                .nextBooking(mapBookingToShortDto(getNextBooking(item.getBookings())))
                .comments(item.getComments() != null ? item.getComments().stream()
                        .map(this::toCommentDto)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }

    private CommentDto toCommentDto(Comment comment) {
        if (comment == null || comment.getAuthor() == null) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    private Booking getLastBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getEnd().isBefore(now))
                .sorted((b1, b2) -> b2.getEnd().compareTo(b1.getEnd()))
                .findFirst()
                .orElse(null);
    }

    private Booking getNextBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getStart().isAfter(now))
                .sorted((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                .findFirst()
                .orElse(null);
    }

    private BookingDto mapBookingToShortDto(Booking booking) {
        return booking != null ? bookingMapper.toBookingShortDto(booking) : null;
    }

    public Item toItem(ItemDto itemDto, User owner) {
        if (itemDto == null) {
            return null;
        }
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .requestId(itemDto.getRequestId())
                .build();
    }
}