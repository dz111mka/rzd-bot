package ru.chepikov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.chepikov.model.Station;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

    Optional<Station> findByName(String name);

    Optional<Station> findFirstByNameIgnoreCase(String name);

    @Query(value = """
            SELECT *
            FROM station
            WHERE lower(name) = lower(:query)
               OR lower(name) LIKE lower(:query) || '%'
               OR lower(name) % lower(:query)
            ORDER BY
                CASE
                    WHEN lower(name) = lower(:query) THEN 0
                    WHEN lower(name) LIKE lower(:query) || '%' THEN 1
                    ELSE 2
                END,
                similarity(lower(name), lower(:query)) DESC,
                name ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Station> searchByName(@Param("query") String query, @Param("limit") int limit);
}