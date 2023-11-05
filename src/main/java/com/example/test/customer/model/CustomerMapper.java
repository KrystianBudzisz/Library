package com.example.test.customer.model;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer fromCreateCommand(CreateCustomerCommand command) {
        Customer customer = new Customer();
        customer.setFirstName(command.getFirstName());
        customer.setLastName(command.getLastName());
        customer.setEmail(command.getEmail());
        return customer;
    }

    public CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setEmailConfirmed(customer.isEmailConfirmed());
        return dto;
    }
}