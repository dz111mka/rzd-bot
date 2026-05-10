package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.repository.TrainSubscriptionRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscriptionService {

    TrainSubscriptionRepository trainSubscriptionRepository;

    public List<TrainSubscription> findAll() {
        return trainSubscriptionRepository.findAll();
    }

    public List<TrainSubscription> findByUserId(long userId) {
        return trainSubscriptionRepository.findByUserId(userId);
    }

    public TrainSubscription save(TrainSubscription subscription) {
        return trainSubscriptionRepository.save(subscription);
    }

    public void deleteById(UUID id) {
        trainSubscriptionRepository.deleteById(id);
    }

    public void deleteExpiredData() {
        trainSubscriptionRepository.deleteExpiredData();
    }
}