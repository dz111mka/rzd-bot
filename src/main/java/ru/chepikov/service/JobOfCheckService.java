package ru.chepikov.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.JobOfCheck;
import ru.chepikov.repository.JobOfCheckRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JobOfCheckService {

    JobOfCheckRepository repository;

    List<JobOfCheck> findAllJobs() {
        return repository.findAll();
    }
}
