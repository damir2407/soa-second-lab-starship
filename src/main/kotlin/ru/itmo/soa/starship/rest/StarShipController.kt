package ru.itmo.soa.starship.rest

import jakarta.inject.Inject
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing
import ru.itmo.soa.starship.rest.data.ErrorResponse
import ru.itmo.soa.starship.rest.data.StarShipCreateRequest
import ru.itmo.soa.starship.service.StarShipService
import java.time.LocalDateTime
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/v1/star-ships")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
open class StarShipController @Inject constructor(private val starShipService: StarShipService) {

    constructor() : this(StarShipService())

    @POST
    open fun createStarShip(starShipCreateRequest: StarShipCreateRequest): Response {
        if (starShipCreateRequest.name.isNullOrBlank()) {
            return Response.status(400).entity(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    "Не удалось создать звездный корабль! Поле name не должно быть пустым!"
                )
            ).build()
        }

        if (starShipCreateRequest.maxSpeed <= 0) {
            return Response.status(400).entity(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    "Не удалось создать звездный корабль! Поле maxSpeed должно быть положительным!"
                )
            ).build()
        }

        return starShipService.createStarship(starShipCreateRequest)
    }

    @POST
    @Path("/{starShipId}/load/{spaceMarineId}")
    open fun loadSpaceMarine(@PathParam("starShipId") starShipId: Long,
                             @PathParam("spaceMarineId") spaceMarineId: Long
    ): Response {
        return starShipService.addMarineToStarship(starShipId, spaceMarineId)
    }

    @POST
    @Path("/{starShipId}/unload-all")
    open fun unloadAllFromStarShip(@PathParam("starShipId") starShipId: Long): Response {
        return starShipService.unloadMarinesFromStarShip(starShipId)
    }

    @GET
    open fun getAllStarShips(): Response {
        return starShipService.getAllStarShips()
    }
}