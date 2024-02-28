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
            val measureAnnotation = classDeclaration.annotations.find { it.shortName.asString() == "Measure" } ?: return
            val baseUnit = measureAnnotation.getArgument("base") as? String ?: error("Base unit is required")
            var measure = classDeclaration.simpleName.asString()
            if (!measure.startsWith("A")) {
                logger.error("Measure names must start with 'A'", classDeclaration)
                return
            }
            measure = measure.substring(1)
            val pkg = classDeclaration.packageName.asString()

            val clazzName = ClassName(pkg, measure)
            val clazz = TypeSpec.classBuilder(clazzName)
                .addOriginatingKSFile(classDeclaration.containingFile!!)
                .addModifiers(KModifier.VALUE)
                .jvmInline()
                .addSuperinterface(COMPARABLE.parameterizedBy(clazzName))

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

            val companion = TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder("ZERO", clazzName)
                        .initializer("%T(0.0)", clazzName)
                        .build()
                )
                .addConversionFrom(clazzName, baseUnit)

            val unitAnnotations = classDeclaration.annotations.filter { it.shortName.asString() == "AlternateUnit" }
            for (unitAnnotation in unitAnnotations) {
                val unit = unitAnnotation.getArgument("name") as? String
                val ratio = unitAnnotation.getArgument("ratio") as? Double
                if (unit == null || ratio == null) {
                    logger.error("Name and ratio are required", classDeclaration)
                    return
                }
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

            val extraImports = mutableListOf<Pair<String, String>>()
            val mulAnnotations = classDeclaration.annotations.filter { it.shortName.asString() == "MultipliesTo" }
            for (mulAnnotation in mulAnnotations) {
                val multiplicand = mulAnnotation.getArgument("multiplicand") as? KSTypeReference
                val product = mulAnnotation.getArgument("product") as? KSTypeReference
                if (multiplicand == null || product == null) {
                    logger.error("Multiplicand and product are required", classDeclaration)
                    return
                }
                val multiplicandClass = multiplicand.resolve().declaration
                val productClass = product.resolve().declaration
                val multiplicandBaseUnit = multiplicandClass.annotations
                    .find { it.shortName.asString() == "Measure" }
                    ?.getArgument("base") as? String
                val productBaseUnit = productClass.annotations.find { it.shortName.asString() == "Measure" }
                    ?.getArgument("base") as? String
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
                extraImports.add("$productType.Companion" to productBaseUnit)

                clazz.addFunction(
                    FunSpec.builder("times")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("other", multiplicandType)
                        .returns(productType)
                        .addStatement("return (%L * other.%L).%L", baseUnit, multiplicandBaseUnit, productBaseUnit)
                        .build()
                )
            }

            val divAnnotations = classDeclaration.annotations.filter { it.shortName.asString() == "DividesTo" }
            for (divAnnotation in divAnnotations) {
                val divisor = divAnnotation.getArgument("divisor") as? KSTypeReference
                val quotient = divAnnotation.getArgument("quotient") as? KSTypeReference
                if (divisor == null || quotient == null) {
                    logger.error("Divisor and quotient are required", classDeclaration)
                    return
                }
                val divisorClass = divisor.resolve().declaration
                val quotientClass = quotient.resolve().declaration
                val divisorBaseUnit = divisorClass.annotations
                    .find { it.shortName.asString() == "Measure" }
                    ?.getArgument("base") as? String
                val quotientBaseUnit = quotientClass.annotations.
                find { it.shortName.asString() == "Measure" }
                    ?.getArgument("base") as? String
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
                extraImports.add("$quotientType.Companion" to divisorBaseUnit)

                clazz.addFunction(
                    FunSpec.builder("div")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("other", divisorType)
                        .returns(quotientType)
                        .addStatement("return (%L / other.%L).%L", baseUnit, divisorBaseUnit, quotientBaseUnit)
                        .build()
                )
            }

            clazz.addType(companion.build())

            val fileSpec = FileSpec.builder(
                pkg,
                "Uom$measure"
            )
                .addType(clazz.build())
                .addImport("$clazzName.Companion", baseUnit)
                .addFunction(
                    FunSpec.builder("abs")
                        .returns(clazzName)
                        .addParameter("value", clazzName)
                        .addStatement("return kotlin.math.abs(value.%L).%L", baseUnit, baseUnit)
                        .build()
                )
            for ((import, unit) in extraImports) {
                fileSpec.addImport(import, unit)
            }
            fileSpec.build().writeTo(generator, false)
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
    val operatorFun = FunSpec.builder(operator)
        .addModifiers(KModifier.OPERATOR)
        .addParameter("scalar", Double::class)
        .returns(className)
        .addStatement("return %T(%L %L scalar)", className, baseUnit, symbol)
    return addFunction(operatorFun.build())
}

fun KSAnnotation.getArgument(name: String): Any? {
    return arguments.find { it.name?.asString() == name }?.value
}