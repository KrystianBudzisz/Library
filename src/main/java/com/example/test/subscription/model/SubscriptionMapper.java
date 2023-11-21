package com.example.test.subscription.model;

import com.example.test.category.BookCategory;
import com.example.test.category.BookCategoryRepository;
import com.example.test.customer.CustomerRepository;
import com.example.test.customer.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SubscriptionMapper {

    private final CustomerRepository customerRepository;
    private final BookCategoryRepository bookCategoryRepository;

    public Subscription fromCreateCommand(CreateSubscriptionCommand command) {
        Subscription subscription = new Subscription();

        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID"));
        subscription.setCustomer(customer);

        subscription.setAuthor(command.getAuthor());

        BookCategory category = bookCategoryRepository.findById(command.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        subscription.setCategory(category);

        return subscription;
    }

    public SubscriptionDTO toDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setCustomerId(subscription.getCustomer().getId());
        dto.setAuthor(subscription.getAuthor());
        dto.setCategoryId(subscription.getCategory().getId());

        return dto;
    }
}
