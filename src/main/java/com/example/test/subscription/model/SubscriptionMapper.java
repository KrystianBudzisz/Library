package com.example.test.subscription.model;

import com.example.test.customer.CustomerRepository;
import com.example.test.customer.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SubscriptionMapper {

    private final CustomerRepository customerRepository;

    public Subscription fromCreateCommand(CreateSubscriptionCommand command) {
        Subscription subscription = new Subscription();

        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID"));

        subscription.setCustomer(customer);
        subscription.setAuthor(command.getAuthor());
        subscription.setCategory(command.getCategory());
        return subscription;
    }

    public SubscriptionDTO toDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(subscription.getId());

        dto.setCustomerId(subscription.getCustomer().getId());
        dto.setAuthor(subscription.getAuthor());
        dto.setCategory(subscription.getCategory());
        return dto;
    }
}
