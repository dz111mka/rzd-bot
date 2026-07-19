package ru.chepikov.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.chepikov.config.FeignConfiguration;
import ru.chepikov.model.dto.suggest.RzdSuggestDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "rzdSuggests", url = "https://ticket.rzd.ru/api/v1/suggests", configuration = FeignConfiguration.class)
public interface RzdSuggestClient {

    @GetMapping
    Map<String, List<RzdSuggestDto>> findStations(@RequestParam("Query") String query,
                                                  @RequestParam("TransportType") String transportType,
                                                  @RequestParam("GroupResults") boolean groupResults,
                                                  @RequestParam("RailwaySortPriority") boolean railwaySortPriority,
                                                  @RequestParam("SynonymOn") int synonymOn,
                                                  @RequestParam("Language") String language);
}
