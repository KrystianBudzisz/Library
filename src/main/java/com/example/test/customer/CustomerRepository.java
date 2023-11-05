package com.example.test.customer;


import com.example.test.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);

    Optional<Customer> findByConfirmationToken(String token);

    Optional<Customer> findByEmail(String email);
}
