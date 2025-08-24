package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "status", expression = "java(booking.getStatus().toString())")
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "item", source = "itemId")
    @Mapping(target = "booker", source = "bookerId")
    Booking toBooking(BookingCreateDto bookingCreateDto, Long itemId, Long bookerId);

    default Item mapItem(Long itemId) {
        Item item = new Item();
        item.setId(itemId);
        return item;
    }

    default User mapUser(Long bookerId) {
        User user = new User();
        user.setId(bookerId);
        return user;
    }
}