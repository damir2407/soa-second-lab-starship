package ru.itmo.soa.starship.rest.data

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.NoArgsConstructor

@NoArgsConstructor
data class CoordinatesResponse(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("x")
    val x: Float,
    @JsonProperty("y")
    val y: Int
)