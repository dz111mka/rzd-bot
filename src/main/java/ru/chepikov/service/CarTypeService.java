package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.CarType;
import ru.chepikov.repository.CarTypeRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CarTypeService {

    CarTypeRepository carTypeRepository;

    public List<CarType> findAll() {
        return carTypeRepository.findAll();
    }
}