package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Adding item for user with id {}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner); // Set the User object instead of ownerId
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }


    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item with id {} for user with id {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));
        if (!existingItem.getOwner().getId().equals(userId)) { // Changed to getOwner().getId()
            throw new UserNotFoundException("User not found");
        }
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.info("Getting item with id {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<Item> getItemsByUserId(Long userId, int page, int size) {
        log.info("Getting items for user with id {} on page {} with size {}", userId, page, size);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Item> itemPage = itemRepository.findByOwnerId(userId, pageable);
        return itemPage.getContent();
    }

    @Override
    public List<Item> searchItems(String text, Pageable pageable) {
        log.info("Searching items with text: {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchItems(text.toLowerCase(), pageable).getContent();
    }

    @Override
    public ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId) {
        log.info("Getting item with id {} and bookings for user with id {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = bookingRepository.findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(itemId, now);
        Booking nextBooking = bookingRepository.findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(itemId, now);

        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId()) // Changed to item.getOwner().getId()
                .lastBooking(lastBooking != null ? ItemMapper.toBookingDto(lastBooking) : null)
                .nextBooking(nextBooking != null ? ItemMapper.toBookingDto(nextBooking) : null)
                .build();
    }

    @Override
    public List<Item> getItemsByRequestId(Long requestId) {
        log.info("Getting items for request with id {}", requestId);
        //TODO: Implement the actual logic of retrieving item from request.
        return Collections.emptyList(); // Placeholder implementation
    }
}