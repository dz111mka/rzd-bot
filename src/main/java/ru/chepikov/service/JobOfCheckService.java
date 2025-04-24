package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chepikov.model.JobOfCheck;
import ru.chepikov.repository.JobOfCheckRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobOfCheckService {

    private final JobOfCheckRepository repository;

    public List<JobOfCheck> findAllJobs() {
        return repository.findAll();
    }

    public List<JobOfCheck> findByUserId(long userId) {
        return repository.findByUserId(userId);
    }

    public JobOfCheck save(JobOfCheck job) {
        return repository.save(job);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }




}
