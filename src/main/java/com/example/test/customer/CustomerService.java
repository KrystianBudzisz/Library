package com.example.test.customer;

import com.example.test.customer.model.CreateCustomerCommand;
import com.example.test.customer.model.Customer;
import com.example.test.customer.model.CustomerDTO;
import com.example.test.customer.model.CustomerMapper;
import com.example.test.email.EmailService;
import com.example.test.exception.BusinessException;
import com.example.test.exception.DatabaseException;
import com.example.test.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EmailService emailService;

    @Transactional
    public CustomerDTO registerCustomer(CreateCustomerCommand command) {

        Customer newCustomer = customerMapper.fromCreateCommand(command);

        newCustomer.setEmailConfirmed(false);
        newCustomer.setConfirmationToken(generateUniqueConfirmationToken());
        newCustomer = customerRepository.save(newCustomer);

        emailService.sendConfirmationEmail(newCustomer.getEmail(), "Email Confirmation", newCustomer.getConfirmationToken());

        return customerMapper.toDTO(newCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(customerMapper::toDTO);
    }

    private String generateUniqueConfirmationToken() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public String confirmEmail(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token must not be null or empty");
        }

        Customer customer = customerRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid confirmation token"));

        if (customer.isEmailConfirmed()) {
            throw new BusinessException("Email is already confirmed");
        }

        customer.setEmailConfirmed(true);

        return "Email successfully confirmed";
    }
}

