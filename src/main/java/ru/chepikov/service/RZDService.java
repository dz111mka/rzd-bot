package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chepikov.feign.RZDApiClient;

import java.time.LocalDate;
import java.util.Map;

@Service
public class RZDService {

    @Autowired
    private RZDApiClient rzdApiClient;

    public String fetchTrainPrices(Integer origin, Integer destination, LocalDate departureDate) {
        return rzdApiClient.getPrices(origin, destination, departureDate);
    }
}
