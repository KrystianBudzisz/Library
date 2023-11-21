package com.example.test.book.model;

import lombok.*;

import java.time.LocalDate;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class BookDTO {
    private Long id;
    private String author;
    private String title;
    private Long categoryId;
    private LocalDate addedDate;
}