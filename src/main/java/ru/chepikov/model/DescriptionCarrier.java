package ru.chepikov.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

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


