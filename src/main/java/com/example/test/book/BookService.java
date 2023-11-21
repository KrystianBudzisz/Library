package com.example.test.book;


import com.example.test.book.model.Book;
import com.example.test.book.model.BookDTO;
import com.example.test.book.model.BookMapper;
import com.example.test.book.model.CreateBookCommand;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@AllArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;


    public Page<BookDTO> getAllBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(bookMapper::toDTO);
    }

    @Transactional
    public BookDTO addBook(CreateBookCommand command) {

        Book newBook = bookMapper.fromCreateCommand(command);
        newBook.setAddedDate(LocalDate.now());
        newBook = bookRepository.save(newBook);
        return bookMapper.toDTO(newBook);
    }


}
