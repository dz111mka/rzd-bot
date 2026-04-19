package ru.chepikov.model.dto.train;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.chepikov.model.dto.carrier.CarGroupsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainDto {

    @JsonProperty("TrainNumber")
    private String trainNumber;

    @JsonProperty("CarGroups")
    private List<CarGroupsDto> carGroupsList;

    @JsonProperty("DepartureDateTime")
    private LocalDateTime departureDateTime;

    @JsonProperty("ArrivalDateTime")
    private LocalDateTime arrivalDateTime;

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public String toFormattedString(int trainNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("──── ПОЕЗД #").append(trainNumber).append(" ────\n")
                .append("│ 🚆 ").append(this.trainNumber).append("\n")
                .append("│ ⏰ Отправление: ").append(departureDateTime.format(timeFormatter)).append("\n")
                .append("│ ⏰ Прибытие: ").append(arrivalDateTime.format(timeFormatter)).append("\n");

        if (carGroupsList != null && !carGroupsList.isEmpty()) {
            sb.append("│ \n")
                    .append("│ Доступные вагоны:\n");

            for (CarGroupsDto car : carGroupsList) {
                if (car.getTotalSeat() > 0) { // показываем только вагоны с местами
                    sb.append("│   └ ").append(car.toFormattedString()).append("\n");
                }
            }
        } else {
            sb.append("│ \n")
                    .append("│ ❌ Нет доступных мест\n");
        }
        sb.append("───────────────────────");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toFormattedString(1); // дефолтный номер поезда
    }
}
