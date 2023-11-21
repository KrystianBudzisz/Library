package com.example.test.book.model;

import com.example.test.category.BookCategory;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book fromCreateCommand(CreateBookCommand command) {
        Book book = new Book();
        book.setAuthor(command.getAuthor());
        book.setTitle(command.getTitle());

        BookCategory category = new BookCategory();
        category.setId(command.getCategoryId());
        book.setCategory(category);

        return book;
    }

    public BookDTO toDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setAuthor(book.getAuthor());
        dto.setTitle(book.getTitle());
        dto.setCategoryId(book.getCategory().getId());
        dto.setAddedDate(book.getAddedDate());
        return dto;
    }
}

