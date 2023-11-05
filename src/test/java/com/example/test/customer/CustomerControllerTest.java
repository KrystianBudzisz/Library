package com.example.test.customer;

import com.example.test.customer.model.CreateCustomerCommand;
import com.example.test.customer.model.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;


    @AfterEach
    void teardown() {
        customerRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testRegisterCustomer() throws Exception {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        String jsonRequest = objectMapper.writeValueAsString(command);

        mockMvc.perform(
                        post("/api/customers/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        assertTrue(customerRepository.existsByEmail("john.doe@example.com"));

        Optional<Customer> registeredCustomer = customerRepository.findByEmail("john.doe@example.com");
        assertTrue(registeredCustomer.isPresent());
        assertEquals("John", registeredCustomer.get().getFirstName());
        assertEquals("Doe", registeredCustomer.get().getLastName());
    }

    @Test
    public void testConfirmEmail() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setEmailConfirmed(false);
        customer.setConfirmationToken(UUID.randomUUID().toString());
        customerRepository.save(customer);

        mockMvc.perform(
                        get("/api/customers/confirm-email")
                                .param("token", customer.getConfirmationToken())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Email successfully confirmed"));

        Optional<Customer> confirmedCustomer = customerRepository.findById(customer.getId());
        assertTrue(confirmedCustomer.isPresent());
        assertTrue(confirmedCustomer.get().isEmailConfirmed());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllCustomers() throws Exception {
        Customer customer1 = new Customer();
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setEmail("john1.doe@example.com");
        customerRepository.save(customer1);

        Customer customer2 = new Customer();
        customer2.setFirstName("Jane");
        customer2.setLastName("Doe");
        customer2.setEmail("jane.doe@example.com");
        customerRepository.save(customer2);

        mockMvc.perform(
                        get("/api/customers/all")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]", hasSize(2)))
                .andExpect(jsonPath("$.content[0].firstName", equalTo("John")))
                .andExpect(jsonPath("$.content[0].lastName", equalTo("Doe")))
                .andExpect(jsonPath("$.content[1].firstName", equalTo("Jane")))
                .andExpect(jsonPath("$.content[1].lastName", equalTo("Doe")));

        long count = customerRepository.count();
        assertEquals(2, count);
        Optional<Customer> foundCustomer1 = customerRepository.findByEmail("john1.doe@example.com");
        assertTrue(foundCustomer1.isPresent());
        assertEquals("John", foundCustomer1.get().getFirstName());
        assertEquals("Doe", foundCustomer1.get().getLastName());

        Optional<Customer> foundCustomer2 = customerRepository.findByEmail("jane.doe@example.com");
        assertTrue(foundCustomer2.isPresent());
        assertEquals("Jane", foundCustomer2.get().getFirstName());
        assertEquals("Doe", foundCustomer2.get().getLastName());
    }


}
