package ru.chepikov.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "train_subscription")
public class TrainSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "origin_station")
    private String originStation;

    @Column(name = "destination_station")
    private String destinationStation;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "hashcode")
    private Integer hashcode;
}