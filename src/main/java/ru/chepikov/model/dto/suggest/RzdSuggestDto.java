package ru.chepikov.model.dto.suggest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RzdSuggestDto {

    private String nodeId;
    private String expressCode;
    private String name;
    private String nodeType;
    private String transportType;
}
