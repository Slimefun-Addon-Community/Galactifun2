package io.github.addoncommunity.galactifun.units.quantities

import io.github.addoncommunity.galactifun.units.Measure
import io.github.addoncommunity.galactifun.units.div
import io.github.addoncommunity.galactifun.units.times
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

abstract class Quantity(
    val name: String,
    val symbol: String,
    val ratio: Double,
    val offset: Double = 0.0
) {
    companion object {
        internal val constructors = mutableMapOf<Class<out Quantity>, MethodHandle>()
    }

    init {
        val thisClass = this::class.java
        if (thisClass !in constructors) {
            constructors[thisClass] = MethodHandles.lookup().findConstructor(
                thisClass, MethodType.methodType(
                    Void.TYPE,
                    String::class.java,
                    String::class.java,
                    Double::class.java,
                    Double::class.java
                )
            ) ?: throw NoSuchMethodError("No (String, String, Double, Double) constructor found for $thisClass")
        }
    }
}

class Length(name: String, symbol: String, ratio: Double, offset: Double = 0.0) :
    Quantity(name, symbol, ratio, offset)

// Sort length before time
operator fun Time.times(other: Length): Product<Length, Time> = other * this
operator fun Measure<Time>.times(other: Length): Measure<Product<Length, Time>> = Measure(value * other.ratio)

val meter = Length("meter", "m", 1.0)
val kilometer = kilo(meter)
val lightYear = Length("light year", "ly", 9.461e12)
val astronomicalUnit = Length("astronomical unit", "AU", 1.496e8)
val mile = Length("mile", "mi", 1609.344)
val foot = Length("foot", "ft", 0.3048)
val inch = Length("inch", "in", 0.0254)

typealias Distance = Length

class Mass(name: String, symbol: String, ratio: Double, offset: Double = 0.0) :
    Quantity(name, symbol, ratio, offset)

// Sort mass before length
operator fun Length.times(other: Mass): Product<Mass, Length> = other * this
operator fun Measure<Length>.times(other: Mass): Measure<Product<Mass, Length>> = Measure(value * other.ratio)

val gram = Mass("gram", "g", 0.001)
val kilogram = kilo(gram)
val pound = Mass("pound", "lb", 0.45359237)

class Time(name: String, symbol: String, ratio: Double, offset: Double = 0.0) :
    Quantity(name, symbol, ratio, offset)

val second = Time("second", "s", 1.0)
val minute = Time("minute", "min", 60.0)
val hour = Time("hour", "h", minute.ratio * 60.0)
val day = Time("day", "d", hour.ratio * 24.0)
val year = Time("year", "yr", day.ratio * 365.25)

class Temperature(name: String, symbol: String, ratio: Double, offset: Double = 0.0) :
    Quantity(name, symbol, ratio, offset)

val kelvin = Temperature("kelvin", "K", 1.0)
val celsius = Temperature("celsius", "°C", 1.0, 273.15)
val fahrenheit = Temperature("fahrenheit", "°F", 5.0 / 9.0, 255.37222222222223)

typealias Velocity = Ratio<Length, Time>
typealias Acceleration = Ratio<Length, Product<Time, Time>>
typealias Force = Product<Mass, Acceleration>

val newton: Force = Product(kilogram, meter / (second * second))

typealias Energy = Product<Force, Length>

val joule: Energy = newton * meter

typealias Power = Ratio<Energy, Time>

val watt: Power = joule / second

typealias Pressure = Ratio<Force, Product<Length, Length>>
typealias Density = Ratio<Mass, Product<Length, Length>>
typealias Volume = Product<Length, Product<Length, Length>>
typealias Area = Product<Length, Length>
typealias Frequency = Inverse<Time>

val hertz: Frequency = Inverse(second)