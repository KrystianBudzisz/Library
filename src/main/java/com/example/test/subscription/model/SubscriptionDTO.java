package com.example.test.subscription.model;


import com.example.test.category.BookCategory;
import lombok.*;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SubscriptionDTO {
    private Long id;
    private Long customerId;
    private String author;
    private BookCategory category;

}