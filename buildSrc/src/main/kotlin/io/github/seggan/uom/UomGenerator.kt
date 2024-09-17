package io.github.seggan.uom

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmInline
import java.math.BigDecimal
import java.util.*
import kotlin.experimental.ExperimentalTypeInference

internal fun UomConfig.generateUomClassForMeasure(measure: Measure, file: FileSpec.Builder) {
    val clazzName = measure.name.className(pkg)
    when (measure) {
        is Measure.Existing -> {
            if (measure.inCompanion) {
                file.addImport("$clazzName.Companion", measure.scalarToUnit)
            }
        }
        is Measure.New -> {
            val clazz = TypeSpec.classBuilder(clazzName)
                .addModifiers(KModifier.VALUE)
                .jvmInline()
                .addSuperinterface(COMPARABLE.parameterizedBy(clazzName))
                .addSuperinterface(Formattable::class)
            if (allowKoltinxSerialization) {
                clazz.addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            }
            val baseUnit = measure.unitToScalar
            clazz.primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.PRIVATE)
                    .addParameter(baseUnit, Double::class)
                    .build()
            )
                .addProperty(
                    PropertySpec.builder(baseUnit, Double::class)
                        .initializer(baseUnit)
                        .build()
                )
                .addOperator("plus", "+", baseUnit, clazzName)
                .addOperator("minus", "-", baseUnit, clazzName)
                .addScalarOperator("times", "*", baseUnit, clazzName)
                .addScalarOperator("div", "/", baseUnit, clazzName)
                .addFunction(
                    FunSpec.builder("div")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("other", clazzName)
                        .returns(Double::class)
                        .addStatement("return %L / other.%L", baseUnit, baseUnit)
                        .build()
                )
                .addScalarOperator("rem", "%", baseUnit, clazzName)
                .addFunction(
                    FunSpec.builder("unaryMinus")
                        .addModifiers(KModifier.OPERATOR)
                        .returns(clazzName)
                        .addStatement("return %T(-%L)", clazzName, baseUnit)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("unaryPlus")
                        .addModifiers(KModifier.OPERATOR)
                        .returns(clazzName)
                        .addStatement("return this")
                        .build()
                )
                .addFunction(
                    FunSpec.builder("compareTo")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("other", clazzName)
                        .returns(Int::class)
                        .addStatement("return %L.compareTo(other.%L)", baseUnit, baseUnit)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addStatement(
                            "return %T(%L.toString()).toPlainString() + %S",
                            BigDecimal::class.java,
                            baseUnit,
                            " ${baseUnit.camelToWords()}"
                        )
                        .build()
                )
                .addFunction(
                    FunSpec.builder("formatTo")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("formatter", Formatter::class)
                        .addParameter("flags", Int::class)
                        .addParameter("width", Int::class)
                        .addParameter("precision", Int::class)
                        .addStatement(
                            "formatter.format(%S + precision + %S, %N)",
                            "%,.",
                            "f ${baseUnit.camelToWords()}",
                            baseUnit,
                        )
                        .build()
                )

            val companion = TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder("ZERO", clazzName)
                        .initializer("%T(0.0)", clazzName)
                        .build()
                )
                .addConversionFrom(clazzName, baseUnit)

            for ((unit, ratio) in measure.units) {
                clazz.addProperty(
                    PropertySpec.builder(unit, Double::class)
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return %L / %L", baseUnit, ratio)
                                .build()
                        )
                        .build()
                )
                companion.addConversionFrom(clazzName, unit, ratio)
            }

            clazz.addType(companion.build())

            file.addImport("$clazzName.Companion", baseUnit)
                .addType(clazz.build())
                .addFunction(
                    FunSpec.builder("abs")
                        .returns(clazzName)
                        .addParameter("value", clazzName)
                        .addStatement("return kotlin.math.abs(value.%L).%L", baseUnit, baseUnit)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("sum")
                        .addAnnotation(AnnotationSpec.builder(JvmName::class)
                            .addMember("%S", "${clazzName.simpleName.lowercase()}Sum")
                            .build()
                        )
                        .returns(clazzName)
                        .receiver(ITERABLE.parameterizedBy(clazzName))
                        .addStatement("return this.fold(%T.ZERO) { acc, value -> acc + value }", clazzName)
                        .build()
                )

            val t = TypeVariableName("T")
            @OptIn(ExperimentalTypeInference::class)
            file.addFunction(
                FunSpec.builder("unitSumOf")
                    .addAnnotation(
                        AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                            .addMember("%T::class", ClassName("kotlin.experimental", "ExperimentalTypeInference"))
                            .build()
                    )
                    .addAnnotation(AnnotationSpec.builder(JvmName::class)
                        .addMember("%S", "${clazzName.simpleName.lowercase()}SumOf")
                        .build()
                    )
                    .addAnnotation(OverloadResolutionByLambdaReturnType::class)
                    .addModifiers(KModifier.INLINE)
                    .addTypeVariable(t)
                    .returns(clazzName)
                    .receiver(ITERABLE.parameterizedBy(t))
                    .addParameter("selector", LambdaTypeName.get(null, t, returnType = clazzName))
                    .addStatement("var sum = %T.ZERO", clazzName)
                    .addStatement("for (element in this) sum += selector(element)")
                    .addStatement("return sum")
                    .build()
            )
        }
    }
}

internal fun UomConfig.generateOperation(operation: Operation, file: FileSpec.Builder) {
    val leftClazz = operation.left.name.className(pkg)
    val rightClazz = operation.right.name.className(pkg)
    val resultClazz = operation.result.name.className(pkg)
    file.addFunction(
        FunSpec.builder("times")
            .addModifiers(KModifier.OPERATOR)
            .receiver(leftClazz)
            .addParameter("other", rightClazz)
            .returns(resultClazz)
            .addStatement("return (%L * other.%L).%L", operation.left.unitToScalar, operation.right.unitToScalar, operation.result.scalarToUnit)
            .build()
    )
    file.addFunction(
        FunSpec.builder("times")
            .addModifiers(KModifier.OPERATOR)
            .receiver(rightClazz)
            .addParameter("other", leftClazz)
            .returns(resultClazz)
            .addStatement("return (%L * other.%L).%L", operation.right.unitToScalar, operation.left.unitToScalar, operation.result.scalarToUnit)
            .build()
    )
    file.addFunction(
        FunSpec.builder("divTo${rightClazz.simpleName}")
            .addModifiers(KModifier.INFIX)
            .receiver(resultClazz)
            .addParameter("other", leftClazz)
            .returns(rightClazz)
            .addStatement("return (this.%L / other.%L).%L", operation.result.unitToScalar, operation.left.unitToScalar, operation.right.scalarToUnit)
            .build()
    )
    file.addFunction(
        FunSpec.builder("divTo${leftClazz.simpleName}")
            .addModifiers(KModifier.INFIX)
            .receiver(resultClazz)
            .addParameter("other", rightClazz)
            .returns(leftClazz)
            .addStatement("return (this.%L / other.%L).%L", operation.result.unitToScalar, operation.right.unitToScalar, operation.left.scalarToUnit)
            .build()
    )
}

private fun String.className(pkg: String): ClassName {
    return if ('.' in this) {
        val parts = split(".")
        ClassName(parts.dropLast(1).joinToString("."), parts.last())
    } else {
        ClassName(pkg, this)
    }
}

private fun TypeSpec.Builder.addConversionFrom(
    clazz: ClassName,
    name: String,
    ratio: Double = 1.0,
): TypeSpec.Builder {
    for (type in listOf(Double::class, Int::class, Long::class)) {
        val prop = PropertySpec.builder(name, clazz)
            .receiver(type)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %T(this * %L)", clazz, ratio)
                    .build()
            )
        addProperty(prop.build())
    }
    return this
}

private fun TypeSpec.Builder.addOperator(
    operator: String,
    symbol: String,
    baseUnit: String,
    className: ClassName
): TypeSpec.Builder {
    val operatorFun = FunSpec.builder(operator)
        .addModifiers(KModifier.OPERATOR)
        .addParameter("other", className)
        .returns(className)
        .addStatement("return %T(%L %L other.%L)", className, baseUnit, symbol, baseUnit)
    return addFunction(operatorFun.build())
}

private fun TypeSpec.Builder.addScalarOperator(
    operator: String,
    symbol: String,
    baseUnit: String,
    className: ClassName
): TypeSpec.Builder {
    for (type in listOf(Double::class, Int::class)) {
        val operatorFun = FunSpec.builder(operator)
            .addModifiers(KModifier.OPERATOR)
            .addParameter("scalar", type)
            .returns(className)
            .addStatement("return %T(%L %L scalar)", className, baseUnit, symbol)
        addFunction(operatorFun.build())
    }
    return this
}

private fun String.camelToWords(): String {
    return replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2].lowercase()}" }
}