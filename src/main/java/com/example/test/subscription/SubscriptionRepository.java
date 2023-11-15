package com.example.test.subscription;


import com.example.test.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.author IN :authors OR s.category.id IN :categories")
    List<Subscription> findByAuthorsOrCategories(@Param("authors") Set<String> authors, @Param("categories") Set<Long> categories);

}