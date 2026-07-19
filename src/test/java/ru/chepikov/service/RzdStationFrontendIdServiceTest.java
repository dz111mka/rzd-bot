package ru.chepikov.service;

import org.junit.jupiter.api.Test;
import ru.chepikov.feign.RzdSuggestClient;
import ru.chepikov.model.Station;
import ru.chepikov.model.dto.suggest.RzdSuggestDto;
import ru.chepikov.repository.StationRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RzdStationFrontendIdServiceTest {

    private final RzdSuggestClient suggestClient = mock(RzdSuggestClient.class);
    private final StationRepository stationRepository = mock(StationRepository.class);
    private final RzdStationFrontendIdService service = new RzdStationFrontendIdService(suggestClient, stationRepository);

    @Test
    void resolvesFrontendIdByExpressCodeAndSavesStation() {
        Station station = new Station(2100170, "Orsha", null);
        RzdSuggestDto suggestion = suggestion("5a8aca65340c742578d36789", "2100170", "station", "train");

        when(suggestClient.findStations("Orsha", "rail,suburban", true, true, 1, "ru"))
                .thenReturn(Map.of("train", List.of(suggestion)));

        assertThat(service.resolveFrontendId(station))
                .contains("5a8aca65340c742578d36789");
        assertThat(station.getFrontendId()).isEqualTo("5a8aca65340c742578d36789");
        verify(stationRepository).save(station);
    }

    @Test
    void prefersTrainStationSuggestionOverCitySuggestion() {
        Station station = new Station(2004000, "Saint Petersburg", null);
        RzdSuggestDto city = suggestion("city-id", "2004000", "city", "city");
        RzdSuggestDto train = suggestion("train-id", "2004000", "station", "train");

        when(suggestClient.findStations("Saint Petersburg", "rail,suburban", true, true, 1, "ru"))
                .thenReturn(Map.of("city", List.of(city), "train", List.of(train)));

        assertThat(service.resolveFrontendId(station)).contains("train-id");
    }

    private RzdSuggestDto suggestion(String nodeId, String expressCode, String nodeType, String transportType) {
        RzdSuggestDto suggestion = new RzdSuggestDto();
        suggestion.setNodeId(nodeId);
        suggestion.setExpressCode(expressCode);
        suggestion.setNodeType(nodeType);
        suggestion.setTransportType(transportType);
        return suggestion;
    }
}
