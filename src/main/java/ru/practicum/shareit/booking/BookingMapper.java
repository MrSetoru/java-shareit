package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

@Component
public class BookingMapper {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingMapper(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setItemId(booking.getItem().getId());
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStatus(booking.getStatus().toString());

        bookingDto.setItemName(booking.getItem().getName());
        bookingDto.setBookerName(booking.getBooker().getName());

        return bookingDto;
    }

    public Booking toBooking(BookingDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }
        if (bookingDto.getItemId() == null) {
            throw new ValidationException("Item ID cannot be null");
        }
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        Booking booking = Booking.builder()
                .item(item)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
        return booking;
    }

    public BookingDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setItemId(booking.getItem().getId());
        dto.setBookerId(booking.getBooker().getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus().toString());
        return dto;
    }
}