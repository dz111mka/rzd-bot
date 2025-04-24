package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.StationInfo;
import ru.chepikov.repository.StationInfoRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationInfoService {

    private final StationInfoRepository repository;

    public StationInfo findStationInfo(String station) {
        return repository.findByName(station)
                .orElseThrow(() -> new RuntimeException("Не найдена станция с названием " + station));
    }
}
