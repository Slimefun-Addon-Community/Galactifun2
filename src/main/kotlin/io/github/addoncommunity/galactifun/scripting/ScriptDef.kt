package io.github.addoncommunity.galactifun.scripting

import io.github.addoncommunity.galactifun.api.objects.TheUniverse
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.base.objects.earth.Earth
import io.github.addoncommunity.galactifun.base.objects.earth.Moon
import io.github.addoncommunity.galactifun.pluginInstance
import org.bukkit.Material
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

@KotlinScript(
    fileExtension = "planet.kts",
    compilationConfiguration = PlanetScriptConfig::class,
    evaluationConfiguration = PlanetScriptEval::class
)
abstract class PlanetScript {
    val eternalDay = DayCycle.ETERNAL_DAY
    val eternalNight = DayCycle.ETERNAL_NIGHT
}

object PlanetScriptConfig : ScriptCompilationConfiguration({
    defaultImports(
        "io.github.addoncommunity.galactifun.util.*",
        "io.github.addoncommunity.galactifun.scripting.dsl.*",
        "io.github.addoncommunity.galactifun.api.objects.properties.Distance.Companion.lightYears",
        "io.github.addoncommunity.galactifun.api.objects.properties.Distance.Companion.kilometers",
        "io.github.addoncommunity.galactifun.api.objects.properties.Distance.Companion.au",
        "kotlin.time.Duration.Companion.hours",
        "kotlin.time.Duration.Companion.days"
    )
    defaultImports(Material::class, BaseUniverse::class, TheUniverse::class, Earth::class, Moon::class)
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

fun evalScript(script: SourceCode): ResultWithDiagnostics<EvaluationResult> {
    return BasicJvmScriptingHost().eval(script, PlanetScriptConfig, PlanetScriptEval)
}