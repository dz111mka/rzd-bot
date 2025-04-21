package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.DescriptionCarrier;

@Repository
public interface DescriptionCarrierRepository extends JpaRepository<DescriptionCarrier, String> {
}
