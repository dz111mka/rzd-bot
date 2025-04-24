package ru.chepikov.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "description_carrier")
public class DescriptionCarrier {

    @Id
    @Column(name = "type", length = 30, nullable = false)
    private String type;

    @Column(name = "description", nullable = false)
    private String description;

    @Override
    public String toString() {
        return description;
    }
}


