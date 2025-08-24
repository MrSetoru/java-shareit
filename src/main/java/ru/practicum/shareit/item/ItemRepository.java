package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Метод для получения Item с загруженным владельцем (User), используется в getItemById
    // Если getItemById не требует owner, можно использовать просто findById
    @Query("SELECT i FROM Item i JOIN FETCH i.owner WHERE i.id = :itemId")
    Optional<Item> findByIdWithOwner(@Param("itemId") Long itemId);

    // Метод для получения всех Item для пользователя с пагинацией
    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);

    // Метод для поиска по тексту с пагинацией
    // Убедись, что этот метод соответствует твоим требованиям к поиску (регистронезависимость, LIKE, доступность)
    @Query("SELECT i FROM Item i WHERE (LOWER(i.name) LIKE LOWER(concat('%', :text, '%')) OR LOWER(i.description) LIKE LOWER(concat('%', :text, '%'))) AND i.available = true")
    Page<Item> searchItems(@Param("text") String text, Pageable pageable);

    // Метод для получения вещей, связанных с запросом (если ItemRequest является отдельной сущностью)
    // List<Item> findByItemRequest(ItemRequest itemRequest);

    // Метод для получения вещей по requestId (если requestId - просто Long в Item)
    List<Item> findByRequestId(Long requestId); // Пример

    // Метод для получения всех вещей с загруженным владельцем (используется редко, для общих операций)
    @Query("SELECT i FROM Item i JOIN FETCH i.owner")
    List<Item> findAllWithOwner();
}