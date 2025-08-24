package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // --- POST /items ---
    // Добавление новой вещи
    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestBody ItemDto itemDto) {
        // Валидация происходит в ItemDto и ItemServiceImpl
        ItemDto createdItem = itemService.addItem(userId, itemDto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    // --- PATCH /items/{itemId} ---
    // Обновление вещи
    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) {
        // userId - тот, кто обновляет (должен быть владельцем)
        ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        return ResponseEntity.ok(updatedItem);
    }

    // --- GET /items/{itemId} ---
    // Получение информации о вещи
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, // userId для общей безопасности, хотя не обязателен для этого GET
                                               @PathVariable Long itemId) {
        // ItemDto не требует userId для получения, но userId может быть нужен для проверки прав доступа
        // В текущей реализации getItemById в сервисе не требует userId.
        // Если бы требовал, то передавали бы его.
        ItemDto item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    // --- GET /items ---
    // Получение списка всех вещей текущего пользователя
    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsByUserId(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from, // Параметр пагинации (отступ)
            @RequestParam(defaultValue = "10") int size  // Параметр пагинации (размер страницы)
    ) {
        // Сервис возвращает List<ItemDto>
        List<ItemDto> items = itemService.getItemsByUserId(userId, from, size);
        return ResponseEntity.ok(items);
    }

    // --- GET /items/search?text={text} ---
    // Поиск вещей по тексту
    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestHeader("X-Sharer-User-Id") Long userId, // userId нужен для контекста, но не для самой логики поиска
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Создаем Pageable для поиска
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemDto> items = itemService.searchItems(text, pageable);
        return ResponseEntity.ok(items);
    }

    // --- GET /items/{itemId}/withBookings ---
    // Получение вещи с информацией о последнем и следующем бронировании
    @GetMapping("/{itemId}/withBookings")
    public ResponseEntity<ItemWithBookingsDto> getItemWithBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId, // ID пользователя, который запрашивает
            @PathVariable Long itemId) {
        ItemWithBookingsDto itemWithBookings = itemService.getItemWithBookings(itemId, userId);
        return ResponseEntity.ok(itemWithBookings);
    }

    // --- GET /items/request/{requestId} ---
    // Получение вещей по запросу (если ItemRequest отдельная сущность, или itemId есть в Item)
    // Пока заглушка, если ItemRequest не реализован
    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<ItemDto>> getItemsByRequestId(
            @RequestHeader("X-Sharer-User-Id") Long userId, // userId нужен для контекста, но не для самой логики поиска
            @PathVariable Long requestId) {
        List<ItemDto> items = itemService.getItemsByRequestId(requestId);
        return ResponseEntity.ok(items);
    }
}