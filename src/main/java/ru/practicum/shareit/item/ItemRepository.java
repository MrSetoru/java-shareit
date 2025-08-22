package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.ItemRequest;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.name) LIKE LOWER(concat('%', :text, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(concat('%', :text, '%')) " +
            "AND i.available = TRUE")
    List<Item> searchItems(@Param("text") String text);

    @Query("SELECT i FROM Item i WHERE (LOWER(i.name) LIKE %:text% OR LOWER(i.description) LIKE %:text%) AND i.available = true")
    Page<Item> searchItems(@Param("text") String text, Pageable pageable);

    List<Item> findByItemRequest(ItemRequest itemRequest);
}