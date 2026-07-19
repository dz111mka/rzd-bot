package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chepikov.feign.RzdSuggestClient;
import ru.chepikov.model.Station;
import ru.chepikov.model.dto.suggest.RzdSuggestDto;
import ru.chepikov.repository.StationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RzdStationFrontendIdService {

    private static final String TRANSPORT_TYPE = "rail,suburban";
    private static final String LANGUAGE = "ru";

    private final RzdSuggestClient rzdSuggestClient;
    private final StationRepository stationRepository;

    @Transactional
    public Optional<String> resolveFrontendId(Station station) {
        if (station.getFrontendId() != null && !station.getFrontendId().isBlank()) {
            return Optional.of(station.getFrontendId());
        }

        Optional<String> frontendId = findFrontendId(station);
        frontendId.ifPresent(value -> {
            station.setFrontendId(value);
            stationRepository.save(station);
        });

        return frontendId;
    }

    private Optional<String> findFrontendId(Station station) {
        try {
            Map<String, List<RzdSuggestDto>> response = rzdSuggestClient.findStations(
                    station.getName(),
                    TRANSPORT_TYPE,
                    true,
                    true,
                    1,
                    LANGUAGE
            );

            return response.values().stream()
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .filter(suggestion -> String.valueOf(station.getId()).equals(suggestion.getExpressCode()))
                    .filter(suggestion -> suggestion.getNodeId() != null && !suggestion.getNodeId().isBlank())
                    .min(Comparator.comparingInt(this::priority))
                    .map(RzdSuggestDto::getNodeId);
        } catch (Exception e) {
            log.warn("Failed to resolve RZD frontend id for station {}: {}", station.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    private int priority(RzdSuggestDto suggestion) {
        if ("train".equals(suggestion.getTransportType()) && "station".equals(suggestion.getNodeType())) {
            return 0;
        }
        if ("city".equals(suggestion.getNodeType())) {
            return 1;
        }
        return 2;
    }
}
