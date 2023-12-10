package ru.itmo.soa.starship.filter

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider


@Provider
class CorsFilter : ContainerResponseFilter {

    override fun filter(requestContext: ContainerRequestContext,
                        responseContext: ContainerResponseContext) {
        responseContext.headers.set(
            "Access-Control-Allow-Origin",
            listOf("*"))
        responseContext.headers.set(
            "Access-Control-Allow-Headers",
            listOf("*"))
        responseContext.headers.set(
            "Access-Control-Allow-Methods",
            listOf("*"))
    }
}