package com.example.test.subscription;

import com.example.test.category.BookCategory;
import com.example.test.customer.CustomerRepository;
import com.example.test.customer.model.Customer;
import com.example.test.exception.BusinessException;
import com.example.test.exception.ResourceNotFoundException;
import com.example.test.subscription.model.CreateSubscriptionCommand;
import com.example.test.subscription.model.Subscription;
import com.example.test.subscription.model.SubscriptionDTO;
import com.example.test.subscription.model.SubscriptionMapper;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Subscription> subscriptionCaptor;


    @BeforeEach
    void setUp() {


    }

    @Test
    public void getAllSubscriptions_returnsExpectedSubscriptions() {
        Pageable pageable = PageRequest.of(0, 5);
        Subscription subscription = new Subscription();
        List<Subscription> subscriptionList = Arrays.asList(subscription);

        Page<Subscription> subscriptionPage = new PageImpl<>(subscriptionList);
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();

        when(subscriptionRepository.findAll(pageable)).thenReturn(subscriptionPage);
        when(subscriptionMapper.toDTO(subscription)).thenReturn(subscriptionDTO);

        Page<SubscriptionDTO> result = subscriptionService.getAllSubscriptions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(subscriptionDTO, result.getContent().get(0));

        verify(subscriptionRepository, times(1)).findAll(pageable);
    }


    @Test
    public void createSubscription_validRequest_createsSubscription() {
        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .customerId(1L)
                .author("Test Author")
                .category(new BookCategory(1L, "Fiction"))
                .build();

        Customer mockedCustomer = new Customer(1L, "John", "Doe", "john.doe@example.com", true, "token", new HashSet<>());
        Subscription expectedSubscription = new Subscription();
        expectedSubscription.setCustomer(mockedCustomer);
        expectedSubscription.setAuthor(command.getAuthor());
        expectedSubscription.setCategory(command.getCategory());
        expectedSubscription.setVersion(1);
        SubscriptionDTO expectedSubscriptionDTO = new SubscriptionDTO();

        when(customerRepository.findById(command.getCustomerId())).thenReturn(Optional.of(mockedCustomer));
        when(subscriptionMapper.fromCreateCommand(command)).thenReturn(expectedSubscription);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(expectedSubscription);
        when(subscriptionMapper.toDTO(any(Subscription.class))).thenReturn(expectedSubscriptionDTO);

        SubscriptionDTO result = subscriptionService.createSubscription(command);

        assertNotNull(result);
        assertEquals(expectedSubscriptionDTO, result);

        verify(subscriptionRepository).save(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();
        assertEquals(command.getAuthor(), savedSubscription.getAuthor());
        assertEquals(command.getCustomerId(), savedSubscription.getCustomer().getId());
        assertEquals(command.getCategory(), savedSubscription.getCategory());
        assertNotNull(savedSubscription.getVersion());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void createSubscription_withNonExistentCustomer_throwsException() {
        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder().customerId(1L)
                .build();

        when(customerRepository.findById(command.getCustomerId())).thenReturn(Optional.empty());

        subscriptionService.createSubscription(command);
    }

    @Test(expected = BusinessException.class)
    public void createSubscription_withoutAuthorOrCategory_throwsException() {
        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .customerId(1L)
                .build();

        when(customerRepository.findById(command.getCustomerId())).thenReturn(Optional.of(new Customer()));

        subscriptionService.createSubscription(command);
    }


    @Test(expected = NullPointerException.class)
    public void createSubscription_processingError_throwsException() {
        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .customerId(1L)
                .author("Test Author")
                .build();

        when(customerRepository.findById(command.getCustomerId())).thenReturn(Optional.of(new Customer()));


        subscriptionService.createSubscription(command);
    }


    @Test(expected = NullPointerException.class)
    public void createSubscription_dataIntegrityViolation_throwsException() {
        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .customerId(1L)
                .author("Test Author")
                .build();

        Subscription subscription = new Subscription();

        when(customerRepository.findById(command.getCustomerId())).thenReturn(Optional.of(new Customer()));
        when(subscriptionMapper.fromCreateCommand(command)).thenReturn(subscription);
        lenient().when(subscriptionRepository.save(subscription)).thenThrow(new DataIntegrityViolationException("subscription_unique_constraint"));

        subscriptionService.createSubscription(command);
    }


    @Test
    public void cancelSubscription_cancelsExistingSubscription() {
        Long subscriptionId = 1L;
        Subscription existingSubscription = mock(Subscription.class);
        Customer customer = mock(Customer.class);

        when(existingSubscription.getCustomer()).thenReturn(customer);
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(existingSubscription));

        subscriptionService.cancelSubscription(subscriptionId);

        verify(customer).removeSubscription(existingSubscription);
        verify(subscriptionRepository).delete(existingSubscription);
    }

    @AfterEach
    void teardown() {
        subscriptionRepository.deleteAll();
    }
}
