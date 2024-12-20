package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.StationInfo;

import java.util.Optional;

@Repository
public interface StationInfoRepository extends JpaRepository<StationInfo, Integer> {

    Optional<StationInfo> findByName(String name);
}
