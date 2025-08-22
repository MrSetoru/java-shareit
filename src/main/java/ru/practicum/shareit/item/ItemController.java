package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items - createItem, userId: {}, itemDto: {}", userId, itemDto);
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<Item> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return itemService.getItemsByUserId(userId, page, size);
    }

    @GetMapping("/search")
    public List<Item> searchItems(@RequestParam String text,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemService.searchItems(text, pageable);
    }

    @GetMapping("/{itemId}/bookings")
    public ItemWithBookingsDto getItemWithBookings(@PathVariable Long itemId,
                                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemWithBookings(itemId, userId);
    }

    @GetMapping("/request/{requestId}")
    public List<Item> getItemsByRequestId(@PathVariable Long requestId) {
        return itemService.getItemsByRequestId(requestId);
    }
}