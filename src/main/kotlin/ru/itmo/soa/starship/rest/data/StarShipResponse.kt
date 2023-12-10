package ru.itmo.soa.starship.rest.data

import lombok.NoArgsConstructor

@NoArgsConstructor
data class StarShipResponse(
    val id: Int,
    val name: String,
    val maxSpeed: Double,
    val spaceMarineId: Long
)