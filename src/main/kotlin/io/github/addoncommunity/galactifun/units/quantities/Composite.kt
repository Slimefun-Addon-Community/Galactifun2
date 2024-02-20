package io.github.addoncommunity.galactifun.units.quantities

class Inverse<Q : Quantity>(val quantity: Q) : Quantity(
    "per ${quantity.name}",
    "1/${quantity.symbol}",
    1 / quantity.ratio,
    -quantity.offset / quantity.ratio
)

class Product<Q1 : Quantity, Q2 : Quantity>(
    val first: Q1,
    val second: Q2
) : Quantity(
    "${first.name} times ${second.name}",
    "${first.symbol} * ${second.symbol}",
    first.ratio * second.ratio,
    first.offset * second.ratio + second.offset * first.ratio
)

typealias Square<Q> = Product<Q, Q>

class Ratio<Q1 : Quantity, Q2 : Quantity>(
    val numerator: Q1,
    val denominator: Q2
) : Quantity(
    "${numerator.name} per ${denominator.name}",
    "${numerator.symbol}/${denominator.symbol}",
    numerator.ratio / denominator.ratio,
    (numerator.offset - denominator.offset) / denominator.ratio
) {
    val reciprocal: Ratio<Q2, Q1> by lazy { Ratio(denominator, numerator) }
}

private fun <Q : Quantity> prefix(base: Q, prefix: String, symbol: String, ratio: Double): Q {
    val constructor = Quantity.constructors[base::class.java]!!
    @Suppress("UNCHECKED_CAST")
    return constructor.invoke(
        prefix + base.name,
        symbol + base.symbol,
        base.ratio * ratio,
        base.offset * ratio
    ) as Q
}

fun <Q : Quantity> quecto(base: Q): Q = prefix(base, "quecto ", "q", 1e-30)
fun <Q : Quantity> ronto(base: Q): Q = prefix(base, "ronto ", "r", 1e-27)
fun <Q : Quantity> yocto(base: Q): Q = prefix(base, "yocto ", "y", 1e-24)
fun <Q : Quantity> zepto(base: Q): Q = prefix(base, "zepto ", "z", 1e-21)
fun <Q : Quantity> atto(base: Q): Q = prefix(base, "atto ", "a", 1e-18)
fun <Q : Quantity> femto(base: Q): Q = prefix(base, "femto ", "f", 1e-15)
fun <Q : Quantity> pico(base: Q): Q = prefix(base, "pico ", "p", 1e-12)
fun <Q : Quantity> nano(base: Q): Q = prefix(base, "nano ", "n", 1e-9)
fun <Q : Quantity> micro(base: Q): Q = prefix(base, "micro ", "μ", 1e-6)
fun <Q : Quantity> milli(base: Q): Q = prefix(base, "milli ", "m", 1e-3)
fun <Q : Quantity> centi(base: Q): Q = prefix(base, "centi ", "c", 1e-2)
fun <Q : Quantity> deci(base: Q): Q = prefix(base, "deci ", "d", 1e-1)
fun <Q : Quantity> deca(base: Q): Q = prefix(base, "deca ", "da", 1e1)
fun <Q : Quantity> hecto(base: Q): Q = prefix(base, "hecto ", "h", 1e2)
fun <Q : Quantity> kilo(base: Q): Q = prefix(base, "kilo ", "k", 1e3)
fun <Q : Quantity> mega(base: Q): Q = prefix(base, "mega ", "M", 1e6)
fun <Q : Quantity> giga(base: Q): Q = prefix(base, "giga ", "G", 1e9)
fun <Q : Quantity> tera(base: Q): Q = prefix(base, "tera ", "T", 1e12)
fun <Q : Quantity> peta(base: Q): Q = prefix(base, "peta ", "P", 1e15)
fun <Q : Quantity> exa(base: Q): Q = prefix(base, "exa ", "E", 1e18)
fun <Q : Quantity> zetta(base: Q): Q = prefix(base, "zetta ", "Z", 1e21)
fun <Q : Quantity> yotta(base: Q): Q = prefix(base, "yotta ", "Y", 1e24)
fun <Q : Quantity> ronna(base: Q): Q = prefix(base, "ronna ", "R", 1e27)
fun <Q : Quantity> quetta(base: Q): Q = prefix(base, "quetta ", "Q", 1e30)
