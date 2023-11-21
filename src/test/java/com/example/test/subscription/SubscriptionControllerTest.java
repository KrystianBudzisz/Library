package com.example.test.subscription;

import com.example.test.book.BookRepository;
import com.example.test.book.model.Book;
import com.example.test.category.BookCategory;
import com.example.test.category.BookCategoryRepository;
import com.example.test.customer.CustomerRepository;
import com.example.test.customer.model.Customer;
import com.example.test.subscription.model.CreateSubscriptionCommand;
import com.example.test.subscription.model.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    private Customer sampleCustomer;
    private Subscription sampleSubscription;
    private Book sampleBook;
    private BookCategory sampleBookCategory;

    @BeforeEach
    void setUp() {

        sampleCustomer = customerRepository.saveAndFlush(Customer.builder()
                .firstName("krystian")
                .lastName("budzisz")
                .email("krybud@gmail.com")
                .emailConfirmed(true)
                .build());
        sampleBookCategory = bookCategoryRepository.saveAndFlush(BookCategory.builder()
                .categoryName("Horror")
                .build());

        sampleBook = bookRepository.saveAndFlush(Book.builder()
                .author("adam")
                .title("mickiewicz")
                .category(sampleBookCategory)
                .build());

        sampleSubscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .author(sampleBook.getAuthor())
                .category(sampleBookCategory)
                .customer(sampleCustomer)
                .build());

    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateSubscription_ValidData_CreatesSubscription() throws Exception {

        Customer testCustomer = customerRepository.saveAndFlush(Customer.builder()
                .firstName("Jan")
                .lastName("Nowak")
                .email("jan.nowak@example.com")
                .emailConfirmed(true)
                .build());

        BookCategory testBookCategory = bookCategoryRepository.saveAndFlush(BookCategory.builder()
                .categoryName("Fantasy")
                .build());

        Book testBook = bookRepository.saveAndFlush(Book.builder()
                .author("Andrzej Sapkowski")
                .title("Wiedźmin")
                .category(testBookCategory)
                .addedDate(LocalDate.now())
                .build());

        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .customerId(testCustomer.getId())
                .author(testBook.getAuthor())
                .categoryId(testBookCategory.getId())
                .build();

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author", is(testBook.getAuthor())))
                .andExpect(jsonPath("$.customerId", is(testCustomer.getId().intValue())))
                .andExpect(jsonPath("$.categoryId", is(testBookCategory.getId().intValue())));

        List<Subscription> subscriptions = subscriptionRepository.findAll();
        assertThat(subscriptions, hasSize(2));
        Subscription newSubscription = subscriptions.stream()
                .filter(sub -> !sub.getId().equals(sampleSubscription.getId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals(testBook.getAuthor(), newSubscription.getAuthor());
        assertNotNull(newSubscription.getCustomer());
        assertNotNull(newSubscription.getCategory());
    }


    @Test
    void testCancelSubscription() throws Exception {
        assertTrue(subscriptionRepository.existsById(sampleSubscription.getId()));

        mockMvc.perform(delete("/api/subscriptions/" + sampleSubscription.getId())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Subscription successfully canceled")));


        assertFalse(subscriptionRepository.existsById(sampleSubscription.getId()));
    }


    @Test
    void testGetAllSubscriptions() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                        .with(user("admin").roles("ADMIN"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.customerId == %d)].author", sampleCustomer.getId()).value(sampleBook.getAuthor()));
    }

    @AfterEach
    void teardown() {
        customerRepository.deleteAll();
        bookRepository.deleteAll();
        bookCategoryRepository.deleteAll();
        subscriptionRepository.deleteAll();
    }
}
