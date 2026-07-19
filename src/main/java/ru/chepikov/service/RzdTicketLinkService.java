package ru.chepikov.service;

import org.springframework.stereotype.Service;
import ru.chepikov.model.Station;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class RzdTicketLinkService {

    private static final String SEARCH_RESULTS_URL = "https://ticket.rzd.ru/searchresults/v/1/%s/%s/%s";

    private final RzdStationFrontendIdService rzdStationFrontendIdService;

    public RzdTicketLinkService(RzdStationFrontendIdService rzdStationFrontendIdService) {
        this.rzdStationFrontendIdService = rzdStationFrontendIdService;
    }

    public Optional<String> buildSearchResultsUrl(Station origin,
                                                  Station destination,
                                                  LocalDate departureDate,
                                                  String trainNumber) {
        Optional<String> originFrontendId = rzdStationFrontendIdService.resolveFrontendId(origin);
        Optional<String> destinationFrontendId = rzdStationFrontendIdService.resolveFrontendId(destination);

        if (originFrontendId.isEmpty() || destinationFrontendId.isEmpty()) {
            return Optional.empty();
        }

        String url = SEARCH_RESULTS_URL.formatted(
                originFrontendId.get(),
                destinationFrontendId.get(),
                departureDate
        );

        if (trainNumber == null || trainNumber.isBlank()) {
            return Optional.of(url);
        }

        return Optional.of(url + "?trainNumber=" + encode(trainNumber));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
