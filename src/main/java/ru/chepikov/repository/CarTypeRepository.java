package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.CarType;

@Repository
public interface CarTypeRepository extends JpaRepository<CarType, String> {
}