package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


import java.time.ZoneId;
import java.util.Collection;

@UtilityClass
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(null);
        return item;
    }

    public static ItemDtoAll toItemDtoAll(Item item,
                                   Collection<CommentDto> comments,
                                   Booking lastBooking,
                                   Booking nextBooking) {
        return new ItemDtoAll(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                item.getOwner() != null ? item.getOwner().getId() : null,
                lastBooking != null ?
                        lastBooking.getEnd().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                nextBooking != null ?
                        nextBooking.getStart().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                comments
        );
    }

    public static ItemDtoToRequest toItemDtoToRequest(Item item) {
        return new ItemDtoToRequest(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }

    public static void updateItemFromDto(ItemDto itemDto, Item item) {
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

}