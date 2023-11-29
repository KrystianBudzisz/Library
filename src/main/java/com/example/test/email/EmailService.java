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
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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


    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyBookUpdates() {
        LocalDate today = LocalDate.now();
        final int pageSize = 10000;
        Page<Book> pageResult;
        Pageable pageable = PageRequest.of(0, pageSize);

        do {
            pageResult = bookRepository.findBooksAddedToday(today, pageable);
            List<Book> newBooks = pageResult.getContent();

            processSubscriptionsInBatches(newBooks);

            pageable = pageResult.nextPageable();
        } while (pageResult.hasNext());
    }


    private void processSubscriptionsInBatches(List<Book> newBooks) {
        int batchSize = 500;
        int page = 0;

        Pageable subscriptionPageable = PageRequest.of(page, batchSize);
        Page<Subscription> subscriptionPage;

        do {
            subscriptionPage = subscriptionRepository.findAll(subscriptionPageable);
            List<Subscription> subscriptions = subscriptionPage.getContent();

            for (Subscription subscription : subscriptions) {
                Set<Book> matchedBooks = matchBooksToSubscription(subscription, newBooks);
                if (!matchedBooks.isEmpty()) {
                    sendBeautifulNewBooksNotification(subscription.getCustomer().getEmail(), new ArrayList<>(matchedBooks));
                }
            }

            subscriptionPageable = subscriptionPage.nextPageable();
            page++;
        } while (subscriptionPage.hasNext());
    }

    private Set<Book> matchBooksToSubscription(Subscription subscription, List<Book> newBooks) {
        Set<Book> matchedBooks = new HashSet<>();
        for (Book book : newBooks) {
            if (book.getAuthor().equals(subscription.getAuthor()) ||
                    book.getCategory().equals(subscription.getCategory())) {
                matchedBooks.add(book);
            }
        }
        return matchedBooks;
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
