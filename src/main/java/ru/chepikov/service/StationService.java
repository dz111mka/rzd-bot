package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.chepikov.model.Station;
import ru.chepikov.repository.StationRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationService {

    StationRepository stationRepository;

    @Cacheable("stations")
    public Station findByName(String station) {
        return stationRepository.findByName(station)
                .orElseThrow(() -> new RuntimeException("Станция не найдена: " + station));
    }

    public Station findById(Integer stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found: " + stationId));
    }

    public List<Station> searchByName(String query, int limit) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        return stationRepository.searchByName(normalizedQuery, limit);
    }
}
