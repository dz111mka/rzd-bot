package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.repository.TrainSubscriptionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscriptionService {

    TrainSubscriptionRepository trainSubscriptionRepository;

    public Page<TrainSubscription> findPage(Pageable pageable) {
        return trainSubscriptionRepository.findAll(pageable);
    }

    public List<TrainSubscription> findByUserId(long userId) {
        return trainSubscriptionRepository.findByUserId(userId);
    }

    public TrainSubscription save(TrainSubscription subscription) {
        return trainSubscriptionRepository.save(subscription);
    }

    @Transactional
    public Optional<TrainSubscription> saveIfAbsent(TrainSubscription subscription) {
        Optional<TrainSubscription> existing = trainSubscriptionRepository
                .findFirstByUserIdAndOriginStationAndDestinationStationAndDepartureDate(
                        subscription.getUserId(),
                        subscription.getOriginStation(),
                        subscription.getDestinationStation(),
                        subscription.getDepartureDate()
                );

        if (existing.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(trainSubscriptionRepository.save(subscription));
    }

    @Transactional
    public boolean updateHashcodeIfCurrent(TrainSubscription subscription, Integer newHash) {
        return trainSubscriptionRepository.updateHashcodeIfCurrent(
                subscription.getId(),
                subscription.getHashcode(),
                newHash
        ) == 1;
    }

    public void deleteById(UUID id) {
        trainSubscriptionRepository.deleteById(id);
    }

    public void deleteExpiredData() {
        trainSubscriptionRepository.deleteExpiredData();
    }
}
