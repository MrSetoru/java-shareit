package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.Booking;


@Mapper(componentModel = "spring", uses = {})
public interface ItemMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    ItemDto toItemDto(Item item);

    Item toItem(ItemDto itemDto);

    @Mapping(target = "ownerId", source = "owner.id")
    ItemWithBookingsDto toItemWithBookingsDto(Item item);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    BookingDto toBookingDto(Booking booking);
}