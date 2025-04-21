package ru.chepikov.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "description_carrier")
public class DescriptionCarrier {

    @Id
    @Column(name = "type", length = 30, nullable = false)
    String type;

    @Column(name = "description", nullable = false)
    String description;

    @Override
    public String toString() {
        return description;
    }
}


