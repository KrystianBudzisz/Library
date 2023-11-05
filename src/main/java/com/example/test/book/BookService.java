package com.example.test.book;


import com.example.test.book.model.Book;
import com.example.test.book.model.BookDTO;
import com.example.test.book.model.BookMapper;
import com.example.test.book.model.CreateBookCommand;
import com.example.test.exception.DatabaseException;
import com.example.test.exception.DuplicateResourceException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        try {

            newBook = bookRepository.save(newBook);
            return bookMapper.toDTO(newBook);
        } catch (DataIntegrityViolationException dive) {

            throw new DuplicateResourceException("A book with this author and title already exists.");
        } catch (Exception e) {

            throw new DatabaseException("An error occurred while trying to save the book");
        }

    }

}