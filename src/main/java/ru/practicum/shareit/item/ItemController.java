package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @Valid @RequestBody ItemDto itemDto) {
        return new ResponseEntity<>(itemService.addItem(userId, itemDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.updateItem(userId, itemId, itemDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, // Теперь ожидает ItemDto
                                               @PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItemById(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.getAllItemsByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        log.info("Searching items containing text: {}", text); // Логируем запрос
        List<Item> items = itemService.searchItems(text);
        return new ResponseEntity<>(items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable Long itemId) {
        itemService.deleteItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{itemId}/comment", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<CommentDto> addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody String text) {

        CommentDto commentDto = CommentDto.builder().text(text).build();
        return new ResponseEntity<>(itemService.addComment(itemId, userId, commentDto), HttpStatus.OK);
    }
}