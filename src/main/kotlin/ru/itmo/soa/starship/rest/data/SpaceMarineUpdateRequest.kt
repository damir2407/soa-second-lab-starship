package ru.itmo.soa.starship.rest.data


data class SpaceMarineUpdateRequest(
    val name: String,
    val coordinates: CoordinatesRequest,
    val health: Long,
    val height: Double,
    val category: AstartesCategory?,
    val weaponType: Weapon,
    val chapter: ChapterRequest,
    val starShipId: Long?
)

data class CoordinatesRequest(
    val x: Float,
    val y: Int
)

data class ChapterRequest(
    val name: String,
    val parentLegion: String,
    val marinesCount: Int,
    val world: String
)
