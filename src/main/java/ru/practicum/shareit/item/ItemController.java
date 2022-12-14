package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получили все вещи");
        return itemService.getAllItems(userId);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                               @PathVariable("id") long id) {
        log.info("Получили вещь id {}", id);
        return itemService.getItemById(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestParam(value = "text") String text) {
        log.info("Нашли указанные вещи");
        return itemService.searchItems(text);
    }

    @PostMapping
    public ItemDto saveItem(@RequestHeader("X-Sharer-User-Id") long userId,
                            @Valid @RequestBody ItemDto itemDto) {
        log.info("Добавили новую вещь");
        return itemService.saveItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody CommentDto commentDto,
                                  @PathVariable("itemId") long itemId) {
        log.info("Добавили новый комментарий");
        return itemService.saveComment(userId, commentDto, itemId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable("id") long id) {
        log.info("Обновили вещь c id: {}", id);
        return itemService.updateItem(userId, itemDto, id);
    }
}
