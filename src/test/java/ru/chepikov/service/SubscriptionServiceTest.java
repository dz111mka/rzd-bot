package ru.chepikov.service;

import org.junit.jupiter.api.Test;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.repository.TrainSubscriptionRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionServiceTest {

    private final TrainSubscriptionRepository repository = mock(TrainSubscriptionRepository.class);
    private final SubscriptionService service = new SubscriptionService(repository);

    @Test
    void saveIfAbsentReturnsEmptyWhenSubscriptionAlreadyExists() {
        TrainSubscription subscription = subscription();
        when(repository.findFirstByUserIdAndOriginStationAndDestinationStationAndDepartureDate(
                subscription.getUserId(),
                subscription.getOriginStation(),
                subscription.getDestinationStation(),
                subscription.getDepartureDate()
        )).thenReturn(Optional.of(subscription));

        assertThat(service.saveIfAbsent(subscription)).isEmpty();
        verify(repository, never()).save(subscription);
    }

    @Test
    void saveIfAbsentSavesNewSubscription() {
        TrainSubscription subscription = subscription();
        when(repository.findFirstByUserIdAndOriginStationAndDestinationStationAndDepartureDate(
                subscription.getUserId(),
                subscription.getOriginStation(),
                subscription.getDestinationStation(),
                subscription.getDepartureDate()
        )).thenReturn(Optional.empty());
        when(repository.save(subscription)).thenReturn(subscription);

        assertThat(service.saveIfAbsent(subscription)).contains(subscription);
    }

    @Test
    void updateHashcodeIfCurrentReturnsTrueForSingleUpdatedRow() {
        TrainSubscription subscription = subscription();
        subscription.setHashcode(100);
        when(repository.updateHashcodeIfCurrent(subscription.getId(), 100, 200)).thenReturn(1);

        assertThat(service.updateHashcodeIfCurrent(subscription, 200)).isTrue();
    }

    @Test
    void updateHashcodeIfCurrentReturnsFalseWhenRowWasAlreadyUpdated() {
        TrainSubscription subscription = subscription();
        subscription.setHashcode(100);
        when(repository.updateHashcodeIfCurrent(subscription.getId(), 100, 200)).thenReturn(0);

        assertThat(service.updateHashcodeIfCurrent(subscription, 200)).isFalse();
    }

    private TrainSubscription subscription() {
        TrainSubscription subscription = new TrainSubscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUserId(123L);
        subscription.setOriginStation("Saint Petersburg");
        subscription.setDestinationStation("Orsha");
        subscription.setDepartureDate(LocalDate.of(2026, 8, 12));
        return subscription;
    }
}
