package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Adding item for user with id {}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Item item = itemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item with id {} for user with id {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("User " + userId + " is not the owner of item with id " + itemId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        if (itemDto.getRequestId() != null) {
            existingItem.setRequestId(itemDto.getRequestId());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public List<Item> searchItems(String text) {
        log.info("Searching items containing text: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text);
        Sort sort = Sort.by(Sort.Direction.DESC, "end");

        for (Item item : items) {
            List<Booking> bookingsList = bookingRepository.findByItemIdAndStatus(item.getId(), BookingStatus.APPROVED, sort);
            Set<Booking> bookings = new HashSet<>(bookingsList);
            item.setBookings(bookings);
        }

        for (Item item : items) {
            List<Comment> commentsList = commentRepository.findByItem_Id(item.getId());
            Set<Comment> comments = new HashSet<>(commentsList);
            item.setComments(comments);
        }

        return items;
    }

    @Override
    @Transactional
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Getting item with id {} for user {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));

        ItemDto itemDto = itemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            Booking lastBooking = bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(itemId, BookingStatus.APPROVED, now)
                    .orElse(null);
            Booking nextBooking = bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                    .orElse(null);

            itemDto.setLastBooking(bookingMapper.toBookingShortDto(lastBooking));
            itemDto.setNextBooking(bookingMapper.toBookingShortDto(nextBooking));
        }

        List<CommentDto> comments = item.getComments().stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(comments);

        return itemDto;
    }

    @Override
    @Transactional
    public List<ItemDto> getAllItemsByUserId(Long userId) {
        log.info("Getting all items for user with id {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        List<Item> items = itemRepository.findByOwnerId(userId);

        return items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        log.info("Deleting item with id {} for user with id {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("User " + userId + " is not the owner of item with id " + itemId);
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Adding comment to item with id {} from user with id {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new ValidationException("User " + userId + " did not rent item " + itemId + " or the rental period has not ended yet.");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text cannot be blank");
        }
        if (commentDto.getText().length() > 2048) {
            throw new ValidationException("Comment text is too long (maximum is 2048 characters)");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);
        return toCommentDto(comment);
    }

    @Transactional
    private CommentDto toCommentDto(Comment comment) {
        if (comment == null || comment.getAuthor() == null) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}