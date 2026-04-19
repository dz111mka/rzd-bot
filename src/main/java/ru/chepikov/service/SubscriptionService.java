package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.repository.TrainSubscriptionRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final TrainSubscriptionRepository repository;

    public List<TrainSubscription> findAll() {
        return repository.findAll();
    }

    public List<TrainSubscription> findByUserId(long userId) {
        return repository.findByUserId(userId);
    }

    public TrainSubscription save(TrainSubscription subscription) {
        return repository.save(subscription);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public void deleteExpiredData() {
        repository.deleteExpiredData();
    }
}