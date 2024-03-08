package io.github.addoncommunity.galactifun.scripting.dsl

import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.units.Angle
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.util.general.RequiredProperty
import kotlinx.datetime.Instant

@PlanetDsl
class OrbitBuilder {

    var parent: CelestialObject by RequiredProperty()
    var semimajorAxis: Distance by RequiredProperty()
    var eccentricity: Double by RequiredProperty()
    var argumentOfPeriapsis: Angle by RequiredProperty()
    var timeOfPeriapsis: Instant by RequiredProperty()

    fun build(): Orbit {
        return Orbit(parent, semimajorAxis, eccentricity, argumentOfPeriapsis, timeOfPeriapsis)
    }
}

inline fun orbit(block: OrbitBuilder.() -> Unit): Orbit {
    return OrbitBuilder().apply(block).build()
}

inline fun PlanetBuilder.orbit(block: OrbitBuilder.() -> Unit) {
    orbit = OrbitBuilder().apply(block).build()
}