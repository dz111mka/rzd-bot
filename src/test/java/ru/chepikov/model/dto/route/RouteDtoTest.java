package ru.chepikov.model.dto.route;

import org.junit.jupiter.api.Test;
import ru.chepikov.model.dto.train.TrainDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RouteDtoTest {

    @Test
    void formatsTrainLinkInline() {
        TrainDto train = new TrainDto("083A", List.of(), LocalDateTime.of(2026, 8, 12, 16, 10), LocalDateTime.of(2026, 8, 13, 2, 21));
        RouteDto route = new RouteDto(List.of(train), "Saint Petersburg", "Orsha", LocalDate.of(2026, 8, 12));

        String result = route.toFormattedString(value -> Optional.of("https://ticket.rzd.ru/searchresults/v/1/from/to/2026-08-12?trainNumber=083A"));

        assertThat(result)
                .contains("│ 🔗 <a href=\"https://ticket.rzd.ru/searchresults/v/1/from/to/2026-08-12?trainNumber=083A\">Открыть на РЖД</a>")
                .doesNotContain("RZD links:");
    }
}
