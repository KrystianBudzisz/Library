package com.example.test.book.model;

import com.example.test.category.BookCategory;
import lombok.*;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class BookDTO {
    private Long id;
    private String author;
    private String title;
    private BookCategory category;

}