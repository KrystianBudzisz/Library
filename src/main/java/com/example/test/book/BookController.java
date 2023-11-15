package com.example.test.book;

import com.example.test.book.model.BookDTO;
import com.example.test.book.model.CreateBookCommand;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@AllArgsConstructor
public class BookController {

    private final BookService bookService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody CreateBookCommand command) {
        BookDTO addedBook = bookService.addBook(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedBook);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }
}
