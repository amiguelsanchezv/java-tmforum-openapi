package com.tmforum.openapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyRef {
    
    @JsonProperty("@type")
    private String type = "PartyRef";
    
    private String id;
    
    private String href;
    
    private String name;
    
    @JsonProperty("@referredType")
    private String referredType;
}
