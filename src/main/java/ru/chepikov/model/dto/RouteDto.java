package ru.chepikov.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.chepikov.model.dto.train.Train;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteDto {

    @JsonProperty("Trains")
    private List<Train> trainList;
    @JsonProperty("OriginStationName")
    private String originStationName;
    @JsonProperty("DestinationStationName")
    private String destinationStationName;
    private LocalDate date;

    @Override
    public String toString() {
        return "Поездка на: " + date + "\n" +
                "Из:  " + originStationName + "\n" +
                "До: " + destinationStationName + "\n" + trainList;

    }
}
