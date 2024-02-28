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

            val clazzName = ClassName(classDeclaration.packageName.asString(), measure)
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

            val unitAnnotation = classDeclaration.annotations.find { it.shortName.asString() == "AlternateUnit" }
            if (unitAnnotation != null) {
                val unit = unitAnnotation.getArgument("name") as? String
                if (unit == null) {
                    logger.error("Unit name is required", classDeclaration)
                    return
                }
                val ratio = unitAnnotation.getArgument("ratio") as? Double
                if (ratio == null) {
                    logger.error("Unit ratio is required", classDeclaration)
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

            clazz.addType(companion.build())

            FileSpec.builder(
                classDeclaration.packageName.asString(),
                "Uom$measure"
            )
                .addType(clazz.build())
                .build()
                .writeTo(generator, false)
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