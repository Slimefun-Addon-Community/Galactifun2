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
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

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
        dependenciesFromCurrentContext(wholeClasspath = true)
        jvmTarget("17")
    }
})

object PlanetScriptEval : ScriptEvaluationConfiguration({
    jvm {
        baseClassLoader(pluginInstance::class.java.classLoader)
    }
})

fun evalScript(script: SourceCode): ResultWithDiagnostics<EvaluationResult> {
    val config = createJvmCompilationConfigurationFromTemplate<PlanetScript>()
    val evalConfig = createJvmEvaluationConfigurationFromTemplate<PlanetScript>()
    return BasicJvmScriptingHost().eval(script, config, evalConfig)
}