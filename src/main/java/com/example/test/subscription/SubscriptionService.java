package com.example.test.subscription;


import com.example.test.customer.CustomerRepository;
import com.example.test.customer.model.Customer;
import com.example.test.exception.BusinessException;
import com.example.test.exception.ResourceNotFoundException;
import com.example.test.subscription.model.CreateSubscriptionCommand;
import com.example.test.subscription.model.Subscription;
import com.example.test.subscription.model.SubscriptionDTO;
import com.example.test.subscription.model.SubscriptionMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final CustomerRepository customerRepository;


    public Page<SubscriptionDTO> getAllSubscriptions(Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionRepository.findAll(pageable);
        return subscriptions.map(subscriptionMapper::toDTO);
    }

    @Transactional
    public SubscriptionDTO createSubscription(CreateSubscriptionCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Subscription newSubscription = subscriptionMapper.fromCreateCommand(command);
        customer.addSubscription(newSubscription);

        newSubscription = subscriptionRepository.save(newSubscription);

        return subscriptionMapper.toDTO(newSubscription);
    }


    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscriptionToRemove = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException("Subscription not found"));

        subscriptionToRemove.getCustomer().removeSubscription(subscriptionToRemove);

        subscriptionRepository.delete(subscriptionToRemove);
    }

}