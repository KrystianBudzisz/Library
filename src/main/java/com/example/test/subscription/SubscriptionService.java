package com.example.test.subscription;


import com.example.test.customer.CustomerRepository;
import com.example.test.customer.model.Customer;
import com.example.test.exception.*;
import com.example.test.subscription.model.CreateSubscriptionCommand;
import com.example.test.subscription.model.Subscription;
import com.example.test.subscription.model.SubscriptionDTO;
import com.example.test.subscription.model.SubscriptionMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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

        if (command.getAuthor() == null && command.getCategory() == null) {
            throw new BusinessException("Either author or category must be provided");
        }

        Subscription newSubscription = subscriptionMapper.fromCreateCommand(command);
        customer.addSubscription(newSubscription);

        try {
            newSubscription = subscriptionRepository.save(newSubscription);
        } catch (DataIntegrityViolationException ex) {
            Throwable rootCause = ex.getMostSpecificCause();
            if (rootCause instanceof ConstraintViolationException) {
                ConstraintViolationException constraintEx = (ConstraintViolationException) rootCause;
                if ("uk_subscription_customer_author_category".equals(constraintEx.getConstraintName())) {
                    throw new DuplicateResourceException("Subscription with these details already exists for the customer");
                }
            }
            throw new DatabaseException("Error occurred while processing subscription information", ex);
        } catch (Exception e) {
            throw new OperationFailedException("Unexpected error while creating the subscription.", e);
        }

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