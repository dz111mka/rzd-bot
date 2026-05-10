package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.TrainSubscription;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainSubscriptionRepository extends JpaRepository<TrainSubscription, UUID> {

    List<TrainSubscription> findByUserId(long userId);

    @Modifying
    @Query(value = "DELETE FROM train_subscription WHERE departure_date < now()", nativeQuery = true)
    void deleteExpiredData();
}