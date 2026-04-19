package ru.chepikov.model.dto.carrier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarGroupsDto {

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

    public String toFormattedString() {
        // Форматируем сервис классы
        String serviceClassStr = serviceClasses != null && !serviceClasses.isEmpty()
                ? serviceClasses.toString()
                : "";

        // Создаем краткую информацию о местах
        String seatsInfo = "";
        if (lowerPlace > 0 || upperPlace > 0 || lowerSidePlace > 0 || upperSidePlace > 0) {
            List<String> seatParts = new ArrayList<>();
            if (lowerPlace > 0) seatParts.add("↓" + lowerPlace);
            if (upperPlace > 0) seatParts.add("↑" + upperPlace);
            if (lowerSidePlace > 0) seatParts.add("↓б" + lowerSidePlace);
            if (upperSidePlace > 0) seatParts.add("↑б" + upperSidePlace);

            seatsInfo = " - " + String.join(" ", seatParts);
        }

        return String.format("%s %s - %d мест%s",
                carTypeName, serviceClassStr, totalSeat, seatsInfo);
    }

    @Override
    public String toString() {
        return toFormattedString();
    }
}
