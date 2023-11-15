package com.example.test.email;

import com.example.test.book.BookRepository;
import com.example.test.book.model.Book;
import com.example.test.config.AppConfig;
import com.example.test.subscription.SubscriptionRepository;
import com.example.test.subscription.model.Subscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender emailSender;

    private final BookRepository bookRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final AppConfig appConfig;


    public void sendConfirmationEmail(String to, String subject, String token) {
        validateEmailParameters(to, subject, token);

        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            String confirmationUrl = appConfig.getBaseUrl() + "/api/customers/confirm-email?token=" + token;

            String htmlContent = "<h1>Witaj!</h1>"
                    + "<p>Dziękujemy za zarejestrowanie się w naszej aplikacji. Proszę potwierdź swój adres email, klikając poniższy link:</p>"
                    + "<a href='" + confirmationUrl + "' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block;'>Potwierdź Email</a>"
                    + "<p>Jeśli to nie byłeś Ty, zignoruj tę wiadomość.</p>"
                    + "<p>Pozdrawiamy,<br>Zespół Twojej Aplikacji</p>";

            helper.setText(htmlContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Problem z wysyłaniem emaila", e);
        }
    }

    private void validateEmailParameters(String to, String subject, String text) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email must not be null or empty");
        }

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject must not be null or empty");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Email text must not be null or empty");
        }
    }


    private Map<String, List<Book>> mapSubscriptionsToBooks(List<Subscription> subscriptions, List<Book> newBooks) {
        Map<String, List<Book>> emailToBooksMap = new HashMap<>();
        for (Subscription subscription : subscriptions) {
            for (Book book : newBooks) {
                if (book.getAuthor().equals(subscription.getAuthor()) || book.getCategory().equals(subscription.getCategory())) {
                    emailToBooksMap.computeIfAbsent(subscription.getCustomer().getEmail(), k -> new ArrayList<>()).add(book);
                }
            }
        }
        return emailToBooksMap;
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyBookUpdates() {
        int page = 0;
        final int size = 10000;

        Page<Book> pageResult;
        LocalDate today = LocalDate.now();
        do {
            pageResult = bookRepository.findBooksAddedToday(today, PageRequest.of(page, size));
            List<Book> newBooks = pageResult.getContent();
            processNewBooks(newBooks);
            page++;
        } while (pageResult.hasNext());
    }


    private void processNewBooks(List<Book> newBooks) {
        Set<String> authors = new HashSet<>();
        Set<Long> categoryIds = new HashSet<>();

        for (Book book : newBooks) {
            authors.add(book.getAuthor());
            categoryIds.add(book.getCategory().getId());
        }

        List<Subscription> subscriptions = subscriptionRepository.findByAuthorsOrCategories(authors, categoryIds);

        Map<String, List<Book>> emailToBooksMap = mapSubscriptionsToBooks(subscriptions, newBooks);

        emailToBooksMap.forEach((email, booksToSend) ->
                CompletableFuture.runAsync(() -> sendBeautifulNewBooksNotification(email, booksToSend)));
    }


    public void sendBeautifulNewBooksNotification(String to, List<Book> books) {
        String subject = "New Books in Our Store!";
        String htmlContent = buildBeautifulBooksNotificationContent(books);

        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Problem sending the email", e);
        }
    }


    private String buildBeautifulBooksNotificationContent(List<Book> books) {
        StringBuilder content = new StringBuilder();

        content.append("<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f7f7f7;'>");
        content.append("<h1 style='color: #2E86C1;'>New Books in Our Store!</h1>");
        content.append("<hr style='border: none; border-bottom: 1px solid #ddd;'>");

        for (Book book : books) {
            content.append("<p><strong>").append(book.getTitle()).append("</strong> by ").append(book.getAuthor())
                    .append(" in category ").append(book.getCategory()).append("</p>");
        }

        content.append("<p>Best Regards,<br>Your App Team</p>");
        content.append("</div>");

        return content.toString();
    }


}
