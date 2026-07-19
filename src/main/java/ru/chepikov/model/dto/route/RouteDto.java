package ru.chepikov.model.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.chepikov.model.dto.train.TrainDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteDto {

    @JsonProperty("Trains")
    private List<TrainDto> trainList;

    @JsonProperty("OriginStationName")
    private String originStationName;

    @JsonProperty("DestinationStationName")
    private String destinationStationName;

    private LocalDate date;

    public String toFormattedString() {
        return toFormattedString(train -> Optional.empty());
    }

    public String toFormattedString(Function<TrainDto, Optional<String>> linkResolver) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚂 Поездка: ").append(date).append("\n")
                .append("📍 Из: ").append(originStationName).append("\n")
                .append("🎯 До: ").append(destinationStationName).append("\n\n")
                .append("Найдено поездов: ").append(trainList != null ? trainList.size() : 0).append("\n\n");

        if (trainList != null && !trainList.isEmpty()) {
            for (int i = 0; i < trainList.size(); i++) {
                TrainDto train = trainList.get(i);
                sb.append(train.toFormattedString(i + 1, linkResolver.apply(train)));
                if (i < trainList.size() - 1) {
                    sb.append("\n─── ✨ ───\n\n");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return toFormattedString();
    }
}
