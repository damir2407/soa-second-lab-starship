package ru.itmo.soa.starship.rest.data

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.NoArgsConstructor

@NoArgsConstructor
data class ChapterResponse(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("parentLegion")
    val parentLegion: String,
    @JsonProperty("marinesCount")
    val marinesCount: Int,
    @JsonProperty("world")
    val world: String
)