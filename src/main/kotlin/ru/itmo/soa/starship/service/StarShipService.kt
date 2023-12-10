package ru.itmo.soa.starship.service

import jakarta.enterprise.context.ApplicationScoped
import ru.itmo.soa.starship.rest.data.ChapterRequest
import ru.itmo.soa.starship.rest.data.CoordinatesRequest
import ru.itmo.soa.starship.rest.data.ErrorResponse
import ru.itmo.soa.starship.rest.data.SpaceMarineResponse
import ru.itmo.soa.starship.rest.data.SpaceMarineUpdateRequest
import ru.itmo.soa.starship.rest.data.StarShipCreateRequest
import ru.itmo.soa.starship.rest.data.StarShipResponse
import java.io.FileInputStream
import java.security.KeyStore
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDateTime
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.ws.rs.BadRequestException
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@ApplicationScoped
open class StarShipService {
    fun createStarship(starShipCreateRequest: StarShipCreateRequest): Response {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        val query = "INSERT INTO starship (name, max_speed) VALUES (?, ?) RETURNING id"

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
            preparedStatement = connection.prepareStatement(query)
            preparedStatement.setString(1, starShipCreateRequest.name)
            preparedStatement.setDouble(2, starShipCreateRequest.maxSpeed)

            resultSet = preparedStatement.executeQuery()
            if (resultSet.next()) {
                return Response.status(200).entity(resultSet.getLong("id")).build()
            }
            return Response.status(500).entity(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    "Не удалось создать звездный корабль!"
                )
            ).build()
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }

    }

    fun addMarineToStarship(starShipId: Long, spaceMarineId: Long): Response {
        getStarshipById(starShipId)
            ?: return Response.status(404).entity(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    "Не удалось найти звездный корабль по такому id!"
                )
            ).build()

        return try {
            val spaceMarine = performGetMarineById(spaceMarineId)

            val spaceMarineUpdateRequest = mapToSpaceMarineUpdateRequest(spaceMarine!!, starShipId)
            performUpdateMarine(spaceMarineId, spaceMarineUpdateRequest)
        } catch (ex: BadRequestException) {
            Response.status(404).entity(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    "Не удалось найти звездного десантника по такому id!"
                )
            ).build()
        }


    }

    fun unloadMarinesFromStarShip(starShipId: Long): Response {
        getStarshipById(starShipId)
            ?: return Response.status(404).entity(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    "Не удалось найти звездный корабль по такому id!"
                )
            ).build()

        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        val query = "UPDATE starship SET spacemarine_id = NULL WHERE id = ?"

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
            preparedStatement = connection.prepareStatement(query)
            preparedStatement.setLong(1, starShipId)

            preparedStatement.executeUpdate()
            return Response.status(204).build()

        } finally {
            preparedStatement?.close()
            connection?.close()
        }
    }

    fun getAllStarShips(): Response {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        val query = "SELECT * FROM starship"
        var resultSet: ResultSet? = null
        val starShips = mutableListOf<StarShipResponse>()

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
            preparedStatement = connection.prepareStatement(query)

            resultSet = preparedStatement.executeQuery()

            while (resultSet.next()) {
                val starship = StarShipResponse(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("max_speed"),
                    resultSet.getLong("spacemarine_id")
                )
                starShips.add(starship)
            }
            return Response.status(200).entity(starShips).build()

        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }
    }


    private fun getStarshipById(id: Long): StarShipResponse? {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        val query = "SELECT * FROM starship WHERE id = ?"

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
            preparedStatement = connection.prepareStatement(query)
            preparedStatement.setLong(1, id)

            resultSet = preparedStatement.executeQuery()
            if (resultSet.next()) {
                val starship = StarShipResponse(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("max_speed"),
                    resultSet.getLong("spacemarine_id")
                )
                return starship
            }
            return null
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }
    }

    private fun createSSLContext(truststorePath: String, truststorePassword: String): SSLContext {
        val truststore = KeyStore.getInstance("PKCS12", "BC")

        FileInputStream(truststorePath).use { truststoreInput -> truststore.load(truststoreInput, truststorePassword.toCharArray()) }
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(truststore)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return sslContext
    }

    private fun createSSLClient(): Client {
        val sslContext = createSSLContext(
            "/keystore.jks",
            "password"
        )
        return ClientBuilder.newBuilder()
            .sslContext(sslContext)
            .build()
    }


    private fun performGetMarineById(id: Long): SpaceMarineResponse? {
        return createSSLClient()
            .target(MARINES_URL)
            .path("/{id}")
            .resolveTemplate("id", id)
            .request(MediaType.APPLICATION_JSON)
            .get(SpaceMarineResponse::class.java)
    }

    private fun performUpdateMarine(spaceMarineId: Long, request: SpaceMarineUpdateRequest): Response {
        return createSSLClient()
            .target(MARINES_URL)
            .path("/{id}")
            .resolveTemplate("id", spaceMarineId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json<Any>(request))
    }

    private fun mapToSpaceMarineUpdateRequest(
        spaceMarine: SpaceMarineResponse,
        starShipId: Long
    ): SpaceMarineUpdateRequest {
        val coordinatesRequest = CoordinatesRequest(
            spaceMarine.coordinates.x,
            spaceMarine.coordinates.y
        )

        val chapterRequest = ChapterRequest(
            spaceMarine.chapter.name,
            spaceMarine.chapter.parentLegion,
            spaceMarine.chapter.marinesCount,
            spaceMarine.chapter.world
        )


        return SpaceMarineUpdateRequest(
            spaceMarine.name,
            coordinatesRequest,
            spaceMarine.health,
            spaceMarine.height,
            spaceMarine.category,
            spaceMarine.weaponType,
            chapterRequest,
            starShipId
        )
    }

    companion object {
        private val MARINES_URL = "https://spacemarine:7008/api/v1/space-marines"
        private val DB_URL = "jdbc:postgresql://postgres:5432/soa"
        private val DB_USER = "postgres"
        private val DB_PASSWORD = "postgres"
    }
}