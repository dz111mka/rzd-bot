package ru.chepikov.model.dto.train;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.chepikov.model.dto.carrier.CarGroups;

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

    @Override
    public String toString() {
        return "\nНомер поезда: " + trainNumber + "\n" + carGroupsList;

    }
}
