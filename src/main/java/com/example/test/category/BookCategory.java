package com.example.test.category;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name")
    private String categoryName;

}


