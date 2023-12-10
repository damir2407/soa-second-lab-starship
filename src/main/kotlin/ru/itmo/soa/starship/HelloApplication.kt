package ru.itmo.soa.starship

import jakarta.enterprise.context.Dependent
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

@Dependent
@ApplicationPath("/api")
open class HelloApplication : Application() {
}