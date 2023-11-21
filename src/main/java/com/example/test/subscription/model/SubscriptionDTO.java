package com.example.test.subscription.model;


import lombok.*;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SubscriptionDTO {
    private Long id;
    private Long customerId;
    private String author;
    private Long categoryId;
}