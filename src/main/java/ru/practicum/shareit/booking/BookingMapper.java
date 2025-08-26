package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.format.DateTimeFormatter;

@Component
public class BookingMapper {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public BookingMapper(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setItemId(booking.getItem().getId());
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStart(booking.getStart().format(formatter));
        bookingDto.setEnd(booking.getEnd().format(formatter));
        bookingDto.setStatus(booking.getStatus().toString());
        return bookingDto;
    }

    public Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));
        User booker = userRepository.findById(bookingDto.getBookerId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public BookingDto toBookingDto(BookingCreateDto bookingCreateDto) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(bookingCreateDto.getItemId());
        bookingDto.setStart(bookingCreateDto.getStart().format(formatter));
        bookingDto.setEnd(bookingCreateDto.getEnd().format(formatter));
        return bookingDto;
    }
}