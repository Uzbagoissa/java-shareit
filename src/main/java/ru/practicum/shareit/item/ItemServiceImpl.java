package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemServiceImpl implements ItemService {
    UserService userService;
    BookingRepository bookingRepository;
    ItemRepository repository;
    CommentRepository commentRepository;


    @Override
    public List<ItemDto> getAllItems(long userId) {
        userValid(userId);
        List<ItemDto> itemDtos = ItemMapper.toListItemDto(repository.findByUserId(userId));
        for (ItemDto itemDto : itemDtos) {
            itemOwnerValid(itemDto, userId, itemDto.getId());
            itemDto.setComments(addComments(itemDto.getId()));
        }
        itemDtos.sort(Comparator.comparing(ItemDto::getId));
        return itemDtos;
    }

    @Override
    public ItemDto getItemById(long userId, long id) {
        itemValid(id);
        ItemDto itemDto = ItemMapper.toItemDto(repository.getById(id));
        itemOwnerValid(itemDto, userId, id);
        itemDto.setComments(addComments(id));
        return itemDto;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return ItemMapper.toListItemDto(repository.searchItems(text));
    }

    @Transactional
    @Override
    public ItemDto saveItem(long userId, ItemDto itemDto) {
        userValid(userId);
        if (itemDto.getAvailable() == null) {
            log.error("?????????? ?????????????? ?????????????? ????????!");
            throw new IncorrectParameterException("?????????? ?????????????? ?????????????? ????????!");
        }
        return ItemMapper.toItemDto(repository.save(ItemMapper.toItem(userId, itemDto)));
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long id) {
        userValid(userId);
        itemValid(id);
        if (repository.getById(id).getUserId() != userId) {
            log.error("???????????????????????? ?? id {} ???? ?????????? ???????????????? ?????? ???????? ?? id {}!", userId, id);
            throw new ForbiddenException("???????????????????????? ?????????????????? ???????????????? ?????????? ????????!");
        }
        Item item = ItemMapper.toItem(userId, itemDto);
        if (itemDto.getName() == null) {
            item.setName(repository.getById(id).getName());
            itemDto.setName(repository.getById(id).getName());
        }
        if (itemDto.getDescription() == null) {
            item.setDescription(repository.getById(id).getDescription());
            itemDto.setDescription(repository.getById(id).getDescription());
        }
        if (itemDto.getAvailable() == null) {
            item.setAvailable(repository.getById(id).getAvailable());
            itemDto.setAvailable(repository.getById(id).getAvailable());
        }
        item.setId(id);
        return ItemMapper.toItemDto(repository.save(item));
    }

    @Transactional
    @Override
    public CommentDto saveComment(long userId, CommentDto commentDto, long itemId) {
        if (bookingRepository.findBookingByUserIdAndItemId(userId, itemId, LocalDateTime.now()).isEmpty()) {
            log.error("???????????????????????? ?? id {} ???? ?????????? ?????????????????? ?????????????????????? ?? ???????? ?? id {}!", userId, itemId);
            throw new IncorrectParameterException("???????? ???????????????????????? ???? ?????????? ?????????????????? ?????????????????????? ?? ???????????? ????????!");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(itemId);
        comment.setAuthor(userId);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);
        commentDto = CommentMapper.toCommentDto(commentRepository.save(comment));
        commentDto.setAuthorName(userService.getUserById(userId).getName());
        return commentDto;
    }

    private List<CommentDto> addComments(long itemId) {
        List<CommentDto> comments = new ArrayList<>();
        for (Comment comment : commentRepository.findAll()) {
            if (comment.getItem().equals(itemId)) {
                CommentDto commentDto = CommentMapper.toCommentDto(comment);
                commentDto.setAuthorName(userService.getUserById(comment.getAuthor()).getName());
                comments.add(commentDto);
            }
        }
        return comments;
    }

    private void itemOwnerValid(ItemDto itemDto, long userId, long id) {
        if (repository.getById(id).getUserId().equals(userId)) {
            itemDto.setLastBooking(bookingRepository.findLastBookingByItemId(id, LocalDateTime.now()));
            itemDto.setNextBooking(bookingRepository.findNextBookingByItemId(id, LocalDateTime.now()));
        }
    }

    private void userValid(long userId) {
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("???????????????????????? ?? id ???? ????????????????????! {}", userId);
            throw new NotFoundException("???????????????????????? ?? id ???? ????????????????????!");
        }
    }

    private void itemValid(long id) {
        if (!repository.findAll().stream()
                .map(Item::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("???????? ?? id ???? ????????????????????! {}", id);
            throw new NotFoundException("???????? ?? id ???? ????????????????????!");
        }
    }

}