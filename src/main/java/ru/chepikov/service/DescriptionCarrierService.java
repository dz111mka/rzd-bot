package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.DescriptionCarrier;
import ru.chepikov.repository.DescriptionCarrierRepository;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DescriptionCarrierService {

    private final DescriptionCarrierRepository repository;

    public List<DescriptionCarrier> findAll() {
        List<DescriptionCarrier> all = repository.findAll();
        return all;
    }
}
