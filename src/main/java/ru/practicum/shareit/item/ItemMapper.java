package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User; // Импорт User

// Если ItemMapper управляется Spring, убедись, что componentModel = "spring"
@Mapper(componentModel = "spring", uses = {})
public interface ItemMapper {

    @Mapping(target = "ownerId", source = "owner.id") // owner.id из Item -> ownerId в ItemDto
    ItemDto toItemDto(Item item);

    //@Mapping(target = "owner", ignore = true) // owner будет установлен отдельно
    Item toItem(ItemDto itemDto);

    @Mapping(target = "ownerId", source = "owner.id")
    ItemWithBookingsDto toItemWithBookingsDto(Item item);

    // Методы для маппинга Booking
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    BookingDto toBookingDto(Booking booking);

    // Если нужен маппинг Item в ItemDto с учетом Bookings (хотя обычноItemWithBookingsDto отдельный)
    // @Mapping(target = "ownerId", source = "owner.id")
    // ItemDto toItemDtoWithBookings(Item item); // Название может быть другим

    // Если нужен маппинг BookingCreateDto в Booking (аналогично BookingMapper)
    // @Mapping(target = "id", ignore = true)
    // @Mapping(target = "item", ignore = true)
    // @Mapping(target = "booker", ignore = true)
    // @Mapping(target = "start", source = "start")
    // @Mapping(target = "end", source = "end")
    // @Mapping(target = "status", ignore = true) // Устанавливается в сервисе
    // Booking toBooking(BookingCreateDto bookingCreateDto);
}