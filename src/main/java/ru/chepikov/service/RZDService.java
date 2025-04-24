package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.chepikov.feign.RZDApiClient;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RZDService {

    private final RZDApiClient rzdApiClient;

    public String fetchTrainPrices(Integer origin, Integer destination, LocalDate departureDate) {
        return rzdApiClient.getPrices(origin, destination, departureDate);
    }
}
