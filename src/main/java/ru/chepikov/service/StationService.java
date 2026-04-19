package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.chepikov.model.Station;
import ru.chepikov.repository.StationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository repository;

    @Cacheable("stations")
    public Station findByName(String station) {
        return repository.findByName(station)
                .orElseThrow(() -> new RuntimeException("Станция не найдена: " + station));
    }
}