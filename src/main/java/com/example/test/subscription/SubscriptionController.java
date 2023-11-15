package com.example.test.subscription;

import com.example.test.subscription.model.CreateSubscriptionCommand;
import com.example.test.subscription.model.SubscriptionDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@AllArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionDTO> createSubscription(@Valid @RequestBody CreateSubscriptionCommand command) {
        SubscriptionDTO createdSubscription = subscriptionService.createSubscription(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<String> cancelSubscription(@PathVariable Long subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.ok("Subscription successfully canceled");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<SubscriptionDTO>> getAllSubscriptions(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<SubscriptionDTO> subscriptions = subscriptionService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(subscriptions);
    }

}


