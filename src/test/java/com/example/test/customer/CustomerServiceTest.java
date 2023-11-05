package com.example.test.customer;

import com.example.test.customer.model.CreateCustomerCommand;
import com.example.test.customer.model.Customer;
import com.example.test.customer.model.CustomerDTO;
import com.example.test.customer.model.CustomerMapper;
import com.example.test.email.EmailService;
import com.example.test.exception.DatabaseException;
import com.example.test.exception.DuplicateResourceException;
import com.example.test.exception.EmailServiceException;
import com.example.test.exception.ResourceNotFoundException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }


    @Test
    public void testRegisterCustomer() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        Customer mappedCustomer = new Customer();
        mappedCustomer.setFirstName("John");
        mappedCustomer.setLastName("Doe");
        mappedCustomer.setEmail("john.doe@example.com");
        mappedCustomer.setEmailConfirmed(false);

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");
        savedCustomer.setEmail("john.doe@example.com");
        savedCustomer.setEmailConfirmed(false);
        savedCustomer.setConfirmationToken("unique-token");

        CustomerDTO expectedDto = new CustomerDTO();
        expectedDto.setId(1L);
        expectedDto.setFirstName("John");
        expectedDto.setLastName("Doe");
        expectedDto.setEmail("john.doe@example.com");
        expectedDto.setEmailConfirmed(false);

        when(customerRepository.existsByEmail(command.getEmail())).thenReturn(false);
        when(customerMapper.fromCreateCommand(command)).thenReturn(mappedCustomer);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setConfirmationToken("unique-token");
            return customer;
        });
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(expectedDto);


        CustomerDTO result = customerService.registerCustomer(command);


        assertEquals(expectedDto, result);
        verify(customerRepository).save(customerCaptor.capture());
        Customer capturedCustomer = customerCaptor.getValue();
        assertEquals("John", capturedCustomer.getFirstName());
        assertEquals("Doe", capturedCustomer.getLastName());
        assertEquals("john.doe@example.com", capturedCustomer.getEmail());
        assertFalse(capturedCustomer.isEmailConfirmed());
        assertEquals("unique-token", capturedCustomer.getConfirmationToken());
        verify(emailService).sendConfirmationEmail(eq("john.doe@example.com"), eq("Email Confirmation"), eq("unique-token"));
    }


    @Test
    public void testRegisterCustomerWithExistingEmail() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(customerRepository.existsByEmail(command.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    public void testRegisterCustomerWithProcessingError() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(customerMapper.fromCreateCommand(command)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    public void testRegisterCustomerWithEmailSendError() {

        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        Customer customer = new Customer();
        customer.setFirstName(command.getFirstName());
        customer.setLastName(command.getLastName());
        customer.setEmail(command.getEmail());
        customer.setConfirmationToken("some-token");

        when(customerRepository.existsByEmail(command.getEmail())).thenReturn(false);
        when(customerMapper.fromCreateCommand(command)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).then(invocation -> {
            Customer savedCustomer = invocation.getArgument(0);
            savedCustomer.setConfirmationToken("some-token");
            return savedCustomer;
        });

        doThrow(new EmailServiceException("Failed to send email")).when(emailService).sendConfirmationEmail(anyString(), anyString(), anyString());

        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> customerService.registerCustomer(command));
        assertEquals("Error occurred while sending confirmation email.", exception.getMessage());

        verify(emailService, times(1)).sendConfirmationEmail(eq("john@example.com"), eq("Email Confirmation"), eq("some-token"));
    }


    @Test
    public void testConfirmEmailWithNullToken() {
        assertThrows(IllegalArgumentException.class, () -> customerService.confirmEmail(null));
    }

    @Test
    public void testConfirmEmailWithEmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> customerService.confirmEmail(""));
    }

    @Test
    public void testConfirmEmailWithInvalidToken() {
        when(customerRepository.findByConfirmationToken("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.confirmEmail("invalid"));
    }

    @Test
    public void testConfirmEmailWithError() {
        when(customerRepository.findByConfirmationToken("valid")).thenReturn(Optional.of(new Customer()));
        when(customerRepository.save(any(Customer.class))).thenThrow(new RuntimeException());

        assertThrows(DatabaseException.class, () -> customerService.confirmEmail("valid"));
    }

    @Test
    public void testGetAllCustomers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(customerRepository.findAll(pageRequest)).thenReturn(Page.empty());

        Page<CustomerDTO> result = customerService.getAllCustomers(pageRequest);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void testRegisterCustomerWithBlankFirstName() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThrows(NullPointerException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    public void testRegisterCustomerWithBlankLastName() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("")
                .email("john@example.com")
                .build();

        assertThrows(NullPointerException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    public void testRegisterCustomerWithInvalidEmail() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email-format")
                .build();

        assertThrows(NullPointerException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    public void testRegisterCustomerWithBlankEmail() {
        CreateCustomerCommand command = CreateCustomerCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .email("")
                .build();

        assertThrows(NullPointerException.class, () -> customerService.registerCustomer(command));
    }
}
