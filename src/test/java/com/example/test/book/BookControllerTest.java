package com.example.test.book;


import com.example.test.book.model.Book;
import com.example.test.book.model.CreateBookCommand;
import com.example.test.category.BookCategory;
import com.example.test.category.BookCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    private Book sampleBook;
    private BookCategory sampleBookCategory;

    @BeforeEach
    void setUp() {


        sampleBookCategory = bookCategoryRepository.saveAndFlush(BookCategory.builder()
                .categoryName("Horror")
                .build());

        sampleBook = bookRepository.saveAndFlush(Book.builder()
                .author("adam")
                .title("mickiewicz")
                .category(sampleBookCategory)
                .build());
    }

    @AfterEach
    void teardown() {
        bookRepository.deleteAll();
        bookCategoryRepository.deleteAll();
    }

    @Test
    void testAddBook() throws Exception {
        BookCategory testBookCategory = bookCategoryRepository.saveAndFlush(BookCategory.builder()
                .categoryName("Fantasy")
                .build());

        CreateBookCommand command = CreateBookCommand.builder()
                .author("Andrzej Sapkowski")
                .title("Wiedźmin")
                .category(testBookCategory)
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("admin").roles("ADMIN"))
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("Andrzej Sapkowski"));

        List<Book> books = bookRepository.findAll();
        assertEquals(2, books.size());
        Book newBook = books.stream()
                .filter(book -> !book.getId().equals(sampleBook.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("Andrzej Sapkowski", newBook.getAuthor());
    }

    @Test
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .with(user("user").roles("USER"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].author").value(sampleBook.getAuthor()));

        List<Book> books = bookRepository.findAll();
        assertEquals(1, books.size());
        assertEquals(sampleBook.getId(), books.get(0).getId());
    }

}




