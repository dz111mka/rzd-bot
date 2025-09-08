package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.JobOfCheck;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobOfCheckRepository extends JpaRepository<JobOfCheck, UUID> {

    List<JobOfCheck> findByUserId(long userId);

    @Modifying
    @Query(value = "DELETE FROM job_of_check WHERE departure_date < now()", nativeQuery = true)
    void deletedExpiredData();
}
