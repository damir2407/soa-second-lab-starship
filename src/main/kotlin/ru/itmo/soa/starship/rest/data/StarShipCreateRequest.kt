package ru.itmo.soa.starship.rest.data

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor


@Data
@NoArgsConstructor
@AllArgsConstructor
open class StarShipCreateRequest(
    val name: String? = "",
    val maxSpeed: Double = 0.0
) {
}
