package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService{
    private final BookingRepository repository;
    private final ItemService itemService;
    private final UserService userService;
    @Override
    public BookingDtoOut saveBooking(long userId, BookingDtoIn bookingDtoIn) {
        userValid(userId);
        if (itemService.getItemById(bookingDtoIn.getItemId()).getAvailable().equals(false)) {
            log.error("Этой вещи нет в наличии!");
            throw new IncorrectParameterException("Этой вещи нет в наличии!");
        }
        if (bookingDtoIn.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Некорректное время окончания аренды!");
            throw new IncorrectParameterException("Некорректное время окончания аренды!");
        }
        if (bookingDtoIn.getEnd().isBefore(bookingDtoIn.getStart())) {
            log.error("Время окончания аренды раньше времени начала аренды!");
            throw new IncorrectParameterException("Время окончания аренды раньше времени начала аренды!");
        }
        if (bookingDtoIn.getStart().isBefore(LocalDateTime.now())) {
            log.error("Некорректное время начала аренды!");
            throw new IncorrectParameterException("Некорректное время начала аренды!");
        }
        Booking booking = BookingMapper.toBooking(userId, bookingDtoIn);
        booking.setStatus(BookingStatus.WAITING);
        repository.save(booking);
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(booking);
        bookingDtoOut.setBooker(userService.getUserById(userId));
        bookingDtoOut.setItem(itemService.getItemById(bookingDtoIn.getItemId()));
        return bookingDtoOut;
    }

    @Override
    public BookingDtoOut getBookingById(long id) {
        bookingValid(id);
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(repository.getById(id));
        bookingDtoOut.setBooker(userService.getUserById(repository.getById(id).getBookerId()));
        bookingDtoOut.setItem(itemService.getItemById(repository.getById(id).getItemId()));
        return bookingDtoOut;
    }

    private void userValid(long userId) {
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователя с таким id не существует! {}", userId);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }

    private void bookingValid(long id) {
        if (!repository.findAll().stream()
                .map(Booking::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Бронирования с таким id не существует! {}", id);
            throw new NotFoundException("Бронирования с таким id не существует!");
        }
    }

}