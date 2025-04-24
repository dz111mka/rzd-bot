package ru.chepikov.model.dto.train;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.chepikov.model.dto.carrier.CarGroups;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Train {

    @JsonProperty("TrainNumber")
    private String trainNumber;

    @JsonProperty("CarGroups")
    private List<CarGroups> carGroupsList;

    @JsonProperty("DepartureDateTime")
    private LocalDateTime departureDateTime;

    @JsonProperty("ArrivalDateTime")
    private LocalDateTime arrivalDateTime;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public String toString() {
        return "\nНомер поезда: " + trainNumber +
                "\nОтправление поезда: " + departureDateTime.format(dateTimeFormatter) +
                "\nПрибытие поезда: " + arrivalDateTime.format(dateTimeFormatter) + "\n" + carGroupsList +
                "\n\n";
    }
}
