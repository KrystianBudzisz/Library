package com.example.test.book.model;

import com.example.test.category.BookCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "books", uniqueConstraints = {@UniqueConstraint(columnNames = {"author", "title"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private BookCategory category;

    @Column(name = "added_date")
    private LocalDate addedDate;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

}