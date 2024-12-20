package ru.chepikov.model.dto.carrier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.StringJoiner;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarGroups {

    @JsonProperty("ServiceClasses")
    private List<String> serviceClasses;
    @JsonProperty("CarTypeName")
    private String carTypeName;
    @JsonProperty("LowerPlaceQuantity")
    private int lowerPlace;
    @JsonProperty("UpperPlaceQuantity")
    private int upperPlace;
    @JsonProperty("LowerSidePlaceQuantity")
    private int lowerSidePlace;
    @JsonProperty("UpperSidePlaceQuantity")
    private int upperSidePlace;
    @JsonProperty("TotalPlaceQuantity")
    private int totalSeat;
    @JsonProperty("Carriers")
    private List<String> carriersList;

    @Override
    public String toString() {
        return "\nВагон типа: " + carTypeName + " " + serviceClasses + "\n" +
                "\t Нижние места: " + lowerPlace + "\n" +
                "\t Верхние места: " + upperPlace + "\n" +
                "\t Нижние боковые места: " + lowerSidePlace + "\n" +
                "\t Верхнее боковые места: " + upperSidePlace + "\n" +
                "\t Всего мест: " + totalSeat + "\n";
    }
}
