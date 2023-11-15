package com.example.test.book;


import com.example.test.book.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE b.addedDate = :addedDate")
    Page<Book> findBooksAddedToday(@Param("addedDate") LocalDate addedDate, Pageable pageable);

}
