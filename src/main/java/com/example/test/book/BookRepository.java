package com.example.test.book;


import com.example.test.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE b.addedDate = CURRENT_DATE")
    List<Book> findBooksAddedToday();
}
