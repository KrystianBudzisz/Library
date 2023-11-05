package com.example.test.subscription;


import com.example.test.category.BookCategory;
import com.example.test.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.author = :author OR s.category = :category")
    List<Subscription> findSubscriptionsByAuthorOrCategory(String author, BookCategory category);


}