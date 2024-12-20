package ru.chepikov.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.chepikov.config.FeignConfiguration;

import java.time.LocalDate;
import java.util.Map;

@FeignClient(name = "rzd", url = "https://ticket.rzd.ru/api/v1/railway-service/prices/train-pricing", configuration = FeignConfiguration.class)
public interface RZDApiClient {

    /*@GetMapping("?origin={origin}&destination={destination}&departureDate={departureDate}T00:00:00")*/
    /*Map<String, Object> getPrices(@PathVariable Integer origin,*/
    /*                              @PathVariable Integer destination,*/
    /*                              @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate);*/

    @GetMapping
    String getPrices(@RequestParam Integer origin,
                                  @RequestParam Integer destination,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate);
}
