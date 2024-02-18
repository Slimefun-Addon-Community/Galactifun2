package io.github.addoncommunity.galactifun.scripting

import io.github.addoncommunity.galactifun.api.objects.TheUniverse
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.pluginInstance
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import kotlin.reflect.KClass
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

@KotlinScript(
    displayName = "Galactifun2 Planet Definition",
    fileExtension = "planet.kts",
    compilationConfiguration = PlanetScriptConfig::class,
    evaluationConfiguration = PlanetScriptEval::class
)
abstract class PlanetScript {
    val eternalDay = DayCycle.ETERNAL_DAY
    val eternalNight = DayCycle.ETERNAL_NIGHT

    val toRegister = mutableListOf<PlanetaryObject>()
}

object PlanetScriptConfig : ScriptCompilationConfiguration({
    defaultImports(
        "io.github.addoncommunity.galactifun.util.*",
        "io.github.addoncommunity.galactifun.scripting.dsl.*",
        "io.github.addoncommunity.galactifun.scripting.dsl.gen.*",
        "io.github.addoncommunity.galactifun.util.units.Distance.Companion.lightYears",
        "io.github.addoncommunity.galactifun.util.units.Distance.Companion.kilometers",
        "io.github.addoncommunity.galactifun.util.units.Distance.Companion.au",
        "kotlin.time.Duration.Companion.hours",
        "kotlin.time.Duration.Companion.days"
    )
    defaultImports(
        Material::class,
        World.Environment::class,
        Biome::class,

        BaseUniverse::class,
        TheUniverse::class,
        Atmosphere::class,
        Gas::class,
    )
    compilerOptions.append("-Xadd-modules=ALL-MODULE-PATH")
    jvm {
        dependenciesFromClassloader(classLoader = pluginInstance::class.java.classLoader, wholeClasspath = true)
        jvmTarget("17")
    }
})

object PlanetScriptEval : ScriptEvaluationConfiguration({
    jvm {
        baseClassLoader(pluginInstance::class.java.classLoader)
    }
})

object PlanetScriptHost : ScriptingHostConfiguration({
    getScriptingClass(object : GetScriptingClassByClassLoader {
        override fun invoke(
            classType: KotlinType,
            contextClassLoader: ClassLoader?,
            hostConfiguration: ScriptingHostConfiguration
        ): KClass<*> {
            val cl = pluginInstance::class.java.classLoader
            val fromClass = classType.fromClass
            if (fromClass != null) {
                if (fromClass.java.classLoader == null) return fromClass // root classloader
                val actualClassLoadersChain = generateSequence(cl) { it.parent }
                if (actualClassLoadersChain.any { it == fromClass.java.classLoader }) return fromClass
            }
            return try {
                pluginInstance::class.java.classLoader.loadClass(classType.typeName).kotlin
            } catch (e: Throwable) {
                throw IllegalArgumentException("unable to load class ${classType.typeName}", e)
            }
        }

        override fun invoke(
            classType: KotlinType,
            contextClass: KClass<*>,
            hostConfiguration: ScriptingHostConfiguration
        ): KClass<*> = invoke(classType, contextClass.java.classLoader, hostConfiguration)
    })
})

fun evalScript(script: SourceCode): ResultWithDiagnostics<EvaluationResult> {
    val compileConfig = createJvmCompilationConfigurationFromTemplate<PlanetScript>(PlanetScriptHost)
    val evalConfig = createJvmEvaluationConfigurationFromTemplate<PlanetScript>(PlanetScriptHost)
    return BasicJvmScriptingHost().eval(script, compileConfig, evalConfig)
}