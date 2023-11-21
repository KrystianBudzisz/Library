package com.example.test.subscription.model;

import com.example.test.category.BookCategory;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "Category ID must not be null")
    private Long categoryId;
}