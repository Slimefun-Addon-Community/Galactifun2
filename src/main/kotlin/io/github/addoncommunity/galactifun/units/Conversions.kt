package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.units.quantities.*

//@formatter:off

//<editor-fold desc="Quantity * quantity" defaultstate="collapsed">
operator fun <A : Quantity, B : Quantity> A.times(other: B): Product<A, B> = Product(this, other)
operator fun <A : Quantity> A.times(other: Inverse<A>): Double = ratio / other.ratio
operator fun <A : Quantity, B : Quantity> A.times(other: Inverse<B>): Ratio<A, B> = this / other.quantity
operator fun <A : Quantity> A.times(other: Inverse<Square<A>>): Measure<Inverse<A>> = ratio / other.quantity.first.ratio / other.quantity.first
operator fun <A : Quantity, B : Quantity> A.times(other: Ratio<B, A>): Measure<B> = ratio / other.denominator.ratio * other.numerator
operator fun <A : Quantity, B : Quantity> Ratio<A, B>.times(other: B): Measure<A> = other.ratio / denominator.ratio * numerator
operator fun <A : Quantity, B : Quantity, C : Quantity> Ratio<A, Product<B, C>>.times(other: B): Measure<Ratio<A, C>> = other.ratio / denominator.first.ratio * (numerator / denominator.second)
operator fun <A : Quantity, B : Quantity, C : Quantity, D : Quantity> Ratio<A, Product<B, C>>.times(other: D): Ratio<Product<A, D>, Product<B, C>> = numerator * other / denominator
operator fun <A : Quantity, B : Quantity> Ratio<A, B>.times(other: Ratio<A, B>): Ratio<Product<A, A>, Product<B, B>> = numerator * other.numerator / (denominator * other.denominator)
operator fun <A : Quantity, B : Quantity> Ratio<A, B>.times(other: Ratio<B, A>): Double = numerator * other.numerator / (denominator * other.denominator)
operator fun <A : Quantity, B : Quantity, C : Quantity, D : Quantity> Ratio<A, B>.times(other: Ratio<C, D>): Ratio<Product<A, C>, Product<B, D>> = numerator * other.numerator / (denominator * other.denominator)
operator fun <A : Quantity> Inverse<A>.times(other: A): Double = ratio * other.ratio
operator fun <A : Quantity, B : Quantity> Inverse<A>.times(other: B): Ratio<B, A> = other * this
operator fun <A : Quantity, B : Quantity> Product<A, B>.times(other: Inverse<A>): Measure<B> = first * other * second
operator fun <A : Quantity, B : Quantity> Product<A, B>.times(other: Inverse<B>): Measure<A> = first * other * second
//</editor-fold>

//<editor-fold desc="Quantity / quantity" defaultstate="collapsed">
operator fun <A: Quantity, B: Quantity> A.div(other: B): Ratio<A, B> = Ratio(this, other)
operator fun <A: Quantity, B: Quantity> A.div(other: Ratio<A, B>): Measure<B> = this * other.reciprocal
operator fun <A: Quantity, B: Quantity> Product<A, B>.div(other: A): Measure<B> = first.ratio / other.ratio * second
operator fun <A: Quantity, B: Quantity> Product<A, B>.div(other: B): Measure<A> = second.ratio / other.ratio * first
operator fun <A: Quantity> Product<A, A>.div(other: A): Measure<A> = first.ratio / other.ratio * second
operator fun <A: Quantity, B: Quantity, C: Quantity> Product<A, B>.div(other: Product<C, B>): Ratio<A, C> = first / other.first
operator fun <A: Quantity, B: Quantity, C: Quantity> Product<A, B>.div(other: Product<C, A>): Ratio<B, C> = second / other.first
operator fun <A: Quantity, B: Quantity, C: Quantity> Product<A, B>.div(other: Product<B, C>): Ratio<A, C> = first / other.second
operator fun <A: Quantity, B: Quantity, C: Quantity> Product<A, B>.div(other: Product<A, C>): Ratio<B, C> = second / other.second
operator fun <A: Quantity, B: Quantity> Product<A, B>.div(other: Product<A, B>): Double = ratio / other.ratio
operator fun <A: Quantity, B: Quantity> Product<A, B>.div(other: Product<B, A>): Double = ratio / other.ratio
operator fun <A: Quantity, B: Quantity> Product<A, B>.div(other: Product<B, B>): Measure<Ratio<A, B>> = second.ratio / other.second.ratio * (first / other.first)
operator fun <A: Quantity, B: Quantity> Ratio<A, B>.div(other: Ratio<A, B>): Double = this * other.reciprocal
operator fun <A: Quantity, B: Quantity> Ratio<A, B>.div(other: Ratio<B, A>): Ratio<Product<A, A>, Product<B, B>> = this * other.reciprocal
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Ratio<A, B>.div(other: Ratio<C, D>): Ratio<Product<A, D>, Product<B, C>> = this * other.reciprocal
operator fun <A: Quantity, B: Quantity> Ratio<A, B>.div(other: B): Ratio<A, Product<B, B>> = numerator / (denominator * other)
operator fun <A: Quantity, B: Quantity> Ratio<Product<A, A>, Product<B, B>>.div(other: A): Measure<Ratio<A, Product<B, B>>> = numerator.first.ratio / other.ratio * (numerator.second / denominator)
operator fun <A: Quantity, B: Quantity, C: Quantity> Ratio<Product<A, A>, Product<B, C>>.div(other: A): Measure<Ratio<A, Product<B, C>>> = numerator.first.ratio / other.ratio * (numerator.second / denominator)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Ratio<Product<A, B>, Product<C, D>>.div(other: A): Measure<Ratio<B, Product<C, D>>> = numerator.first.ratio / other.ratio * (numerator.second / denominator)
operator fun <A: Quantity, B: Quantity> Ratio<A, B>.div(other: Ratio<A, Square<B>>): Measure<B> = other.denominator / denominator * (numerator.ratio / other.numerator.ratio)
operator fun <A: Quantity> Inverse<A>.div(other: A): Measure<Inverse<Square<A>>> = ratio * other.ratio * 1 / Square(other, other)
//</editor-fold>

//<editor-fold desc="Measure * measure" defaultstate="collapsed">
operator fun <A: Quantity, B: Quantity> Measure<A>.times(other: Measure<B>): Measure<Product<A, B>> = Measure(value * other.value)
operator fun <A: Quantity, B: Quantity> Measure<A>.times(other: Measure<Ratio<B, A>>): Measure<B> = Measure(value * other.value)
operator fun <A: Quantity, B: Quantity> Measure<A>.times(other: Measure<Inverse<B>>): Measure<Ratio<A, B>> = Measure(value * other.value)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.times(other: Measure<B>): Measure<A> = Measure(value * other.value)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.times(other: Measure<Ratio<A, B>>): Measure<Ratio<Product<A, A>, Product<B, B>>> = Measure(value * other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Ratio<A, Product<B, C>>>.times(other: Measure<B>): Measure<Ratio<A, C>> = Measure(value * other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Measure<Ratio<A, Product<B, C>>>.times(other: Measure<D>): Measure<Ratio<Product<A, D>, Product<B, C>>> = Measure(value * other.value)
operator fun <A: Quantity> Measure<A>.times(other: Measure<Inverse<Square<A>>>): Measure<Inverse<A>> = Measure(value * other.value)
operator fun <A: Quantity> Measure<Inverse<Square<A>>>.times(other: Measure<A>): Measure<Inverse<A>> = Measure(value * other.value)
operator fun <A: Quantity> Measure<Inverse<A>>.times(other: Measure<A>): Double = value * other.value
operator fun <A: Quantity> Measure<A>.times(other: Measure<Inverse<A>>): Double = other.value * value
operator fun <A: Quantity> Measure<Inverse<A>>.times(other: Measure<Inverse<A>>): Measure<Inverse<Square<A>>> = Measure(value * other.value)
operator fun <A: Quantity> Measure<A>.rem(other: Measure<A>): Double = value % other.value
//</editor-fold>

//<editor-fold desc="Measure / measure" defaultstate="collapsed">
operator fun <A: Quantity> Measure<A>.div(other: Measure<A>): Double = value / other.value
operator fun <A: Quantity, B: Quantity> Measure<A>.div(other: Measure<B>): Measure<Ratio<A, B>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: Measure<A>): Measure<B> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: Measure<B>): Measure<A> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Measure<Product<C, B>>): Measure<Ratio<A, C>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Measure<Product<C, A>>): Measure<Ratio<B, C>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Measure<Product<B, C>>): Measure<Ratio<A, C>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Measure<Product<A, C>>): Measure<Ratio<B, C>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: Measure<Product<A, A>>): Measure<Ratio<B, A>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: Measure<Product<B, B>>): Measure<Ratio<A, B>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.div(other: Measure<B>): Measure<Ratio<A, Product<B, B>>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Measure<Ratio<A, B>>.div(other: Measure<Ratio<C, D>>): Measure<Ratio<Product<A, D>, Product<B, C>>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<Product<A, A>, Product<B, B>>>.div(other: Measure<A>): Measure<Ratio<A, Product<B, B>>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Ratio<Product<A, A>, Product<B, C>>>.div(other: Measure<A>): Measure<Ratio<A, Product<B, C>>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Measure<Ratio<Product<A, B>, Product<C, D>>>.div(other: Measure<A>): Measure<Ratio<B, Product<C, D>>> = Measure(value / other.value)
operator fun <A: Quantity, B: Quantity> Measure<A>.div(other: Measure<Ratio<A, B>>): Measure<B> = Measure(value / other.value)
operator fun <A: Quantity> Measure<Inverse<A>>.div(other: Measure<Inverse<Square<A>>>): Measure<A> = Measure(value / other.value)
operator fun <A: Quantity> Measure<Inverse<A>>.div(other: Measure<A>): Measure<Inverse<Square<A>>> = Measure(value / other.value)
//</editor-fold>

//<editor-fold desc="Measure * quantity" defaultstate="collapsed">
operator fun <A: Quantity> Measure<Inverse<A>>.times(other: A): Double = value * other.ratio
operator fun <A: Quantity> Measure<A>.times(other: Inverse<A>): Double = value * other.ratio
operator fun <A: Quantity, B: Quantity> Measure<Inverse<A>>.times(other: B): Measure<Ratio<B, A>> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<A>.times(other: B): Measure<Product<A, B>> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<A>.times(other: Ratio<B, A>): Measure<B> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<A>.times(other: Inverse<B>): Measure<Ratio<A, B>> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.times(other: B): Measure<A> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.times(other: Ratio<A, B>): Measure<Ratio<Product<A, A>, Product<B, B>>> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Ratio<A, Product<B, C>>>.times(other: B): Measure<Ratio<A, C>> = Measure(value * other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Measure<Ratio<A, Product<B, C>>>.times(other: D): Measure<Ratio<Product<A, D>, Product<B, C>>> = Measure(value * other.ratio)
//</editor-fold>

//<editor-fold desc="Measure / quantity" defaultstate="collapsed">
operator fun <A: Quantity> Measure<A>.div(other: A): Double = value / other.ratio
operator fun <A: Quantity, B: Quantity> Measure<A>.div(other: B): Measure<Ratio<A, B>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: A): Measure<B> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: B): Measure<A> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Product<C, B>): Measure<Ratio<A, C>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Product<C, A>): Measure<Ratio<B, C>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Product<B, C>): Measure<Ratio<A, C>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Product<A, B>>.div(other: Product<A, C>): Measure<Ratio<B, C>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: Product<A, A>): Measure<Ratio<B, A>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Product<A, B>>.div(other: Product<B, B>): Measure<Ratio<A, B>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.div(other: B): Measure<A> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Measure<Ratio<A, B>>.div(other: Ratio<C, D>): Measure<Ratio<Product<A, D>, Product<B, C>>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<A, B>>.div(other: Ratio<A, Square<B>>): Measure<B> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<Ratio<Product<A, A>, Product<B, B>>>.div(other: A): Measure<Ratio<A, Product<B, B>>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity> Measure<Ratio<Product<A, A>, Product<B, C>>>.div(other: A): Measure<Ratio<A, Product<B, C>>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity, C: Quantity, D: Quantity> Measure<Ratio<Product<A, B>, Product<C, D>>>.div(other: A): Measure<Ratio<B, Product<C, D>>> = Measure(value / other.ratio)
operator fun <A: Quantity, B: Quantity> Measure<A>.div(other: Ratio<A, B>): Measure<B> = Measure(value / other.ratio)
//</editor-fold>

//@formatter:on