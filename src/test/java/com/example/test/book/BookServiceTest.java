package com.example.test.book;

import com.example.test.book.model.Book;
import com.example.test.book.model.BookDTO;
import com.example.test.book.model.BookMapper;
import com.example.test.book.model.CreateBookCommand;
import com.example.test.category.BookCategory;
import com.example.test.exception.DatabaseException;
import com.example.test.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private BookCategory createSampleBookCategory() {
        return BookCategory.builder()
                .categoryName("Fiction")
                .build();
    }

    @Test
    void getAllBooksReturnsPageOfBooks() {
        Page<Book> mockPage = new PageImpl<>(Collections.singletonList(new Book()));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Page<BookDTO> result = bookService.getAllBooks(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(bookRepository, times(1)).findAll(Pageable.unpaged());
    }

    @Test
    void addBookSuccessfullyCreatesBook() {
        CreateBookCommand createBookCommand = CreateBookCommand.builder()
                .title("Sample Title")
                .author("AuthorName")
                .category(createSampleBookCategory())
                .build();

        Book sampleBook = Book.builder()
                .title("Sample Title")
                .author("AuthorName")
                .category(createSampleBookCategory())
                .build();

        when(bookMapper.fromCreateCommand(any())).thenReturn(sampleBook);
        when(bookRepository.save(any())).thenReturn(sampleBook);
        when(bookMapper.toDTO(any())).thenReturn(BookDTO.builder().title("Sample Title").author("AuthorName").build());

        BookDTO result = bookService.addBook(createBookCommand);

        assertEquals("Sample Title", result.getTitle());
        assertEquals("AuthorName", result.getAuthor());
        verify(bookRepository).save(any());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        Book savedBook = bookCaptor.getValue();

        assertNotNull(savedBook.getCategory());
        assertEquals("Fiction", savedBook.getCategory().getCategoryName());
    }


    @Test
    void addBookThrowsExceptionWhenDataIntegrityIsViolated() {
        CreateBookCommand command = new CreateBookCommand("AuthorName", "BookTitle", new BookCategory());
        Book book = new Book();

        when(bookMapper.fromCreateCommand(command)).thenReturn(book);
        when(bookRepository.save(book)).thenThrow(new DataIntegrityViolationException("Error"));

        assertThrows(DuplicateResourceException.class, () -> bookService.addBook(command));
    }

    @Test
    void addBookThrowsDatabaseExceptionOnOtherErrors() {
        CreateBookCommand command = new CreateBookCommand("AuthorName", "BookTitle", new BookCategory());
        Book book = new Book();

        when(bookMapper.fromCreateCommand(command)).thenReturn(book);
        when(bookRepository.save(book)).thenThrow(new RuntimeException("Some other error"));

        assertThrows(DatabaseException.class, () -> bookService.addBook(command));
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsBlank() {
        CreateBookCommand commandWithBlankAuthor = new CreateBookCommand("", "ValidTitle", new BookCategory());

        assertThrows(NullPointerException.class, () -> bookService.addBook(commandWithBlankAuthor));

        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        CreateBookCommand commandWithBlankTitle = new CreateBookCommand("ValidAuthor", "", new BookCategory());

        assertThrows(NullPointerException.class, () -> bookService.addBook(commandWithBlankTitle));

        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsNull() {
        CreateBookCommand commandWithNullCategory = new CreateBookCommand("ValidAuthor", "ValidTitle", null);

        assertThrows(NullPointerException.class, () -> bookService.addBook(commandWithNullCategory));

        verifyNoInteractions(bookRepository);
    }
}