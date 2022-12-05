package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private long id;
    @NotNull
    @NotBlank(message = "Ошибка: имя пустое или содержит только пробелы")
    private String name;
    @NotNull
    @NotBlank(message = "Ошибка: описание пустое или содержит только пробелы")
    private String description;
    @AssertTrue(message = "Предмета нет в наличии")
    private boolean available;
}
