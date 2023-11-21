package com.example.test.book.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class CreateBookCommand {

    @NotBlank(message = "Author must not be blank")
    private String author;

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotNull(message = "Category ID must not be null")
    private Long categoryId;
}