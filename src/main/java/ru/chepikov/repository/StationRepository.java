package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.Station;

import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

    Optional<Station> findByName(String name);
}