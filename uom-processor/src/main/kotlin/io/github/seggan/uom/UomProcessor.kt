package io.github.seggan.uom

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmInline
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo
import java.math.BigDecimal
import java.util.*
import kotlin.experimental.ExperimentalTypeInference

class UomProcessor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Measure::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        if (!symbols.iterator().hasNext()) return emptyList()
        symbols.filter(KSNode::validate).forEach { it.accept(Visitor(), Unit) }
        return symbols.filterNot(KSNode::validate).toList()
    }

    @Suppress("DuplicatedCode")
    inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            var measure = classDeclaration.simpleName.asString()
            if (!measure.startsWith("A")) {
                logger.error("Measure names must start with 'A'", classDeclaration)
                return
            }
            measure = measure.substring(1)
            val baseUnit = classDeclaration.getBaseUnit() ?: run {
                logger.error("Base unit is required", classDeclaration)
                return
            }
            val pkg = classDeclaration.packageName.asString()

            val clazzName = ClassName(pkg, measure)
            val clazz = TypeSpec.classBuilder(clazzName)
                .addOriginatingKSFile(classDeclaration.containingFile!!)
                .addModifiers(KModifier.VALUE)
                .jvmInline()
                .addSuperinterface(COMPARABLE.parameterizedBy(clazzName))
                .addSuperinterface(Formattable::class)

                .primaryConstructor(
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

            val unitAnnotations = classDeclaration.annotations.filter { it.shortName.asString() == "AlternateUnit" }
            for (unitAnnotation in unitAnnotations) {
                val unit = unitAnnotation.arguments[0].value as String
                val ratio = unitAnnotation.arguments[1].value as Double
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

            val file = FileSpec.builder(
                pkg,
                "Uom$measure"
            )
                .addImport("$clazzName.Companion", baseUnit)
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
                        .returns(clazzName)
                        .receiver(ITERABLE.parameterizedBy(clazzName))
                        .addStatement("return this.fold(%T.ZERO) { acc, value -> acc + value }", clazzName)
                        .build()
                )
            val t = TypeVariableName("T")
            @OptIn(ExperimentalTypeInference::class)
            file.addFunction(
                FunSpec.builder("sumBy")
                    .addAnnotation(
                        AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                            .addMember("%T::class", ClassName("kotlin.experimental", "ExperimentalTypeInference"))
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

            val extraImports = mutableListOf<Pair<String, String>>()
            val mulAnnotations = classDeclaration.annotations.filter { it.shortName.asString() == "MultipliesTo" }
            for (mulAnnotation in mulAnnotations) {
                val multiplicand = mulAnnotation.arguments[0].value as KSType
                val product = mulAnnotation.arguments[1].value as KSType
                val multiplicandClass = multiplicand.declaration
                val productClass = product.declaration
                val multiplicandBaseUnit = multiplicandClass.getBaseUnit()
                val productBaseUnit = productClass.getBaseUnit()
                if (multiplicandBaseUnit == null || productBaseUnit == null) {
                    logger.error("Multiplicand and product must be measures", classDeclaration)
                    return
                }

                val multiplicandType = ClassName(
                    multiplicandClass.packageName.asString(),
                    multiplicandClass.simpleName.asString().substring(1)
                )
                val productType = ClassName(
                    productClass.packageName.asString(),
                    productClass.simpleName.asString().substring(1)
                )
                extraImports.add("$multiplicandType.Companion" to multiplicandBaseUnit)
                extraImports.add("$productType.Companion" to productBaseUnit)

                file.addFunction(
                    FunSpec.builder("times")
                        .addModifiers(KModifier.OPERATOR)
                        .receiver(clazzName)
                        .addParameter("other", multiplicandType)
                        .returns(productType)
                        .addStatement("return (%L * other.%L).%L", baseUnit, multiplicandBaseUnit, productBaseUnit)
                        .build()
                )
                file.addFunction(
                    FunSpec.builder("times")
                        .addModifiers(KModifier.OPERATOR)
                        .receiver(multiplicandType)
                        .addParameter("other", clazzName)
                        .returns(productType)
                        .addStatement("return (%L * other.%L).%L", multiplicandBaseUnit, baseUnit, productBaseUnit)
                        .build()
                )
            }

            val divAnnotations = classDeclaration.annotations.filter { it.shortName.asString() == "DividesTo" }
            for (divAnnotation in divAnnotations) {
                val divisor = divAnnotation.arguments[0].value as KSType
                val quotient = divAnnotation.arguments[1].value as KSType
                val divisorClass = divisor.declaration
                val quotientClass = quotient.declaration
                val divisorBaseUnit = divisorClass.getBaseUnit()
                val quotientBaseUnit = quotientClass.getBaseUnit()
                if (divisorBaseUnit == null || quotientBaseUnit == null) {
                    logger.error("Divisor and quotient must be measures", classDeclaration)
                    return
                }

                val divisorType = ClassName(
                    divisorClass.packageName.asString(),
                    divisorClass.simpleName.asString().substring(1)
                )
                val quotientType = ClassName(
                    quotientClass.packageName.asString(),
                    quotientClass.simpleName.asString().substring(1)
                )
                extraImports.add("$divisorType.Companion" to divisorBaseUnit)
                extraImports.add("$quotientType.Companion" to quotientBaseUnit)

                file.addFunction(
                    FunSpec.builder("div")
                        .addModifiers(KModifier.OPERATOR)
                        .receiver(clazzName)
                        .addParameter("other", divisorType)
                        .returns(quotientType)
                        .addStatement("return (%L / other.%L).%L", baseUnit, divisorBaseUnit, quotientBaseUnit)
                        .build()
                )
                file.addFunction(
                    FunSpec.builder("div")
                        .addModifiers(KModifier.OPERATOR)
                        .receiver(clazzName)
                        .addParameter("other", quotientType)
                        .returns(divisorType)
                        .addStatement("return (%L / other.%L).%L", baseUnit, quotientBaseUnit, divisorBaseUnit)
                        .build()
                )
            }

            for ((import, unit) in extraImports) {
                file.addImport(import, unit)
            }
            file.build().writeTo(generator, false)
        }
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

private fun KSDeclaration.getBaseUnit(): String? {
    return annotations.find { it.shortName.asString() == "Measure" }
        ?.arguments?.firstOrNull()?.value as? String
}

private fun String.camelToWords(): String {
    return replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2].lowercase()}" }
}