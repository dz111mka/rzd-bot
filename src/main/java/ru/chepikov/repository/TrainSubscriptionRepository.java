package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.TrainSubscription;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrainSubscriptionRepository extends JpaRepository<TrainSubscription, UUID> {

    List<TrainSubscription> findByUserId(long userId);

    @Modifying
    @Query(value = "DELETE FROM train_subscription WHERE departure_date < now()", nativeQuery = true)
    void deleteExpiredData();

    @Modifying
    @Query("""
            UPDATE TrainSubscription subscription
            SET subscription.hashcode = :newHash
            WHERE subscription.id = :id
              AND (subscription.hashcode = :oldHash OR (subscription.hashcode IS NULL AND :oldHash IS NULL))
            """)
    int updateHashcodeIfCurrent(@Param("id") UUID id,
                                @Param("oldHash") Integer oldHash,
                                @Param("newHash") Integer newHash);

    Optional<TrainSubscription> findFirstByUserIdAndOriginStationAndDestinationStationAndDepartureDate(
            Long userId,
            String originStation,
            String destinationStation,
            java.time.LocalDate departureDate
    );
}
