package ru.chepikov.service;

import org.junit.jupiter.api.Test;
import ru.chepikov.model.Station;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RzdTicketLinkServiceTest {

    private final RzdStationFrontendIdService frontendIdService = mock(RzdStationFrontendIdService.class);
    private final RzdTicketLinkService service = new RzdTicketLinkService(frontendIdService);

    @Test
    void buildsSearchResultsUrlWithEncodedTrainNumber() {
        Station origin = new Station(2004000, "Saint Petersburg", "5a3244bc340c7441a0a556ca");
        Station destination = new Station(2000000, "Moscow", "5a323c29340c7441a0a556bb");
        when(frontendIdService.resolveFrontendId(origin)).thenReturn(Optional.of("5a3244bc340c7441a0a556ca"));
        when(frontendIdService.resolveFrontendId(destination)).thenReturn(Optional.of("5a323c29340c7441a0a556bb"));

        assertThat(service.buildSearchResultsUrl(origin, destination, LocalDate.of(2026, 7, 20), "759A"))
                .contains("https://ticket.rzd.ru/searchresults/v/1/5a3244bc340c7441a0a556ca/5a323c29340c7441a0a556bb/2026-07-20?trainNumber=759A");
    }

    @Test
    void returnsEmptyWhenFrontendIdIsMissing() {
        Station origin = new Station(2004000, "Saint Petersburg", null);
        Station destination = new Station(2000000, "Moscow", "5a323c29340c7441a0a556bb");
        when(frontendIdService.resolveFrontendId(origin)).thenReturn(Optional.empty());
        when(frontendIdService.resolveFrontendId(destination)).thenReturn(Optional.of("5a323c29340c7441a0a556bb"));

        assertThat(service.buildSearchResultsUrl(origin, destination, LocalDate.of(2026, 7, 20), "759A"))
                .isEmpty();
    }
}
