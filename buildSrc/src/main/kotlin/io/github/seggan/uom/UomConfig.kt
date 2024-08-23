package io.github.seggan.uom

class UomConfig {

    lateinit var pkg: String
    var allowKoltinxSerialization = false

    internal val measures = mutableListOf<Measure>()
    internal val operations = mutableListOf<Operation>()

    fun measure(name: String, base: String, block: Measure.New.() -> Unit = {}): Measure {
        val measure = Measure.New(name, base)
        measure.block()
        measures.add(measure)
        return measure
    }

    fun existingMeasure(name: String, base: String): Measure {
        val measure = Measure.Existing(name, base)
        measures.add(measure)
        return measure
    }

    infix fun Measure.times(other: Measure) = PartialOperation.Multiply(this, other)
    infix fun Measure.dividedBy(other: Measure) = PartialOperation.Divide(this, other)
    infix fun PartialOperation.resultsIn(result: Measure) {
        operations.add(Operation(this, result))
    }
}

sealed interface Measure {
    val name: String
    val base: String

    data class New(override val name: String, override val base: String) : Measure {
        internal val units = mutableMapOf<String, Double>()

        fun unit(name: String, ratio: Double) {
            units[name] = ratio
        }
    }

    data class Existing(override val name: String, override val base: String) : Measure
}

sealed interface PartialOperation {
    data class Multiply(val first: Measure, val second: Measure) : PartialOperation
    data class Divide(val first: Measure, val second: Measure) : PartialOperation
}

data class Operation(val partial: PartialOperation, val result: Measure)