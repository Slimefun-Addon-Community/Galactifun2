package io.github.seggan.uom

class UomConfig {

    lateinit var pkg: String
    val imports = mutableListOf<String>()
    var allowKoltinxSerialization = false

    internal val measures = mutableListOf<Measure>()
    internal val operations = mutableListOf<Operation>()

    fun measure(name: String, base: String, block: Measure.New.() -> Unit = {}): Measure {
        val measure = Measure.New(name, base)
        measure.block()
        measures.add(measure)
        return measure
    }

    fun existingMeasure(fqName: String, unitToScalar: String, block: Measure.Existing.() -> Unit = {}): Measure {
        val measure = Measure.Existing(fqName, unitToScalar)
        measure.block()
        measures.add(measure)
        return measure
    }

    infix fun Measure.times(other: Measure) = PartialOperation(this, other)
    infix fun PartialOperation.resultsIn(result: Measure) {
        operations.add(Operation(left, right, result))
    }
}

sealed interface Measure {
    val name: String
    val unitToScalar: String
    var scalarToUnit: String

    data class New(override val name: String, override val unitToScalar: String) : Measure {

        override var scalarToUnit = unitToScalar
        internal val units = mutableMapOf<String, Double>()

        fun unit(name: String, ratio: Double) {
            units[name] = ratio
        }
    }

    data class Existing(
        override val name: String,
        override val unitToScalar: String,
    ) : Measure {

        override var scalarToUnit = unitToScalar
        internal var inCompanion = false

        fun scalarToUnit(unit: String, inCompanion: Boolean = false) {
            scalarToUnit = unit
            this.inCompanion = inCompanion
        }
    }
}

data class PartialOperation(val left: Measure, val right: Measure)

data class Operation(val left: Measure, val right: Measure, val result: Measure)