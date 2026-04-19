package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.CarType;
import ru.chepikov.repository.CarTypeRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarTypeService {

    private final CarTypeRepository repository;

    public List<CarType> findAll() {
        return repository.findAll();
    }
}