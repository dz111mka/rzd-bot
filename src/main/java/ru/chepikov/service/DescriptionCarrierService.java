package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.DescriptionCarrier;
import ru.chepikov.repository.DescriptionCarrierRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DescriptionCarrierService {

    private final DescriptionCarrierRepository repository;

    public List<DescriptionCarrier> findAll() {
        return repository.findAll();
    }
}
