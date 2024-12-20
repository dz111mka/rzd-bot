package ru.chepikov.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobOfCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    UUID id;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "origin_station")
    String originStation;

    @Column(name = "destination_station")
    String destinationStation;

    @Column(name = "departure_date")
    LocalDate departureDate;

    @Column(name = "hashcode")
    Integer hashcode;
}
