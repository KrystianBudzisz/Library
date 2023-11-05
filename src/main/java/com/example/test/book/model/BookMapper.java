package com.example.test.book.model;

import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book fromCreateCommand(CreateBookCommand command) {
        Book book = new Book();
        book.setAuthor(command.getAuthor());
        book.setTitle(command.getTitle());
        book.setCategory(command.getCategory());
        return book;
    }

    public BookDTO toDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setAuthor(book.getAuthor());
        dto.setTitle(book.getTitle());
        dto.setCategory(book.getCategory());
        return dto;
    }
}

