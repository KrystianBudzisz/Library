package com.example.test.subscription.model;

import com.example.test.category.BookCategory;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateSubscriptionCommand {
    @NotNull(message = "Customer ID must not be null")
    private Long customerId;

    private String author;

    private BookCategory category;
}