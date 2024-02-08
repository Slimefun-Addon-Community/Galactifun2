package io.github.addoncommunity.galactifun.core.space

import io.github.addoncommunity.galactifun.util.buildRandomizedSet
import io.github.addoncommunity.galactifun.util.floodSearchFaces
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import kotlin.math.roundToInt
import kotlin.random.asKotlinRandom

internal object SpacePopulator : BlockPopulator() {

    private val asteroids = ThreadLocal.withInitial {
        buildRandomizedSet {
            add(AsteroidType.TYPE_C, 40f)
            add(AsteroidType.TYPE_S, 30f)
            add(AsteroidType.TYPE_M, 10f)
            add(AsteroidType.COMET, 20f)
        }
    }

    override fun populate(
        worldInfo: WorldInfo,
        random: java.util.Random,
        chunkX: Int,
        chunkZ: Int,
        limitedRegion: LimitedRegion
    ) {
        val rand = random.asKotlinRandom()
        val yRange = worldInfo.minHeight until worldInfo.maxHeight
        val cx = chunkX * 16
        val cz = chunkZ * 16
        repeat(rand.nextInt(4)) {
            val x = cx + rand.nextInt(16)
            val z = cz + rand.nextInt(16)
            val y = yRange.random(rand)
            val size = magicSelectorFunction(random.nextDouble()).coerceAtMost(1024)

            val searched = mutableSetOf<Location>()
            var toSearch = mutableSetOf(Location(null, x.toDouble(), y.toDouble(), z.toDouble()))
            var toSearchNext = mutableSetOf<Location>()

            outer@while (true) {
                if (toSearch.isEmpty()) break
                for (pos in toSearch) {
                    if (pos in searched || !limitedRegion.isInRegion(pos)) continue
                    searched.add(pos)
                    if (searched.size >= size) break@outer
                    for (face in floodSearchFaces) {
                        if (rand.nextBoolean()) continue // Forces asteroids to have a more irregular shape
                        val next = pos.clone().add(face.modX.toDouble(), face.modY.toDouble(), face.modZ.toDouble())
                        if (next !in searched && next !in toSearch && next !in toSearchNext) {
                            toSearchNext.add(next)
                        }
                    }
                }
                toSearch = toSearchNext
                toSearchNext = mutableSetOf()
            }

            // Make sure the asteroid is not too small
            if (random.nextDouble(10.0) > 10 - searched.size) {
                val materials = asteroids.get().getRandom(random).materials.get()
                for (pos in searched) {
                    val material = materials.getRandom(random)
                    limitedRegion.setType(pos, material)
                }
            }
        }
    }

    private enum class AsteroidType(vararg materials: Pair<Material, Int>) {
        TYPE_C(
            Material.DEEPSLATE to 70,
            Material.DEEPSLATE_COAL_ORE to 20,
            Material.DEEPSLATE_IRON_ORE to 4,
            Material.COAL_BLOCK to 5,
            Material.DEEPSLATE_DIAMOND_ORE to 1
        ),
        TYPE_S(
            Material.STONE to 50,
            Material.ANDESITE to 30,
            Material.DIORITE to 19,
            Material.IRON_ORE to 1
        ),
        TYPE_M(
            Material.STONE to 40,
            Material.IRON_ORE to 40,
            Material.RAW_IRON_BLOCK to 20
        ),
        COMET(
            Material.PACKED_ICE to 50,
            Material.BLUE_ICE to 30,
            Material.ICE to 20
        ),
        ;

        val materials = ThreadLocal.withInitial {
            buildRandomizedSet {
                for ((material, weight) in materials) {
                    add(material, weight.toFloat())
                }
            }
        }
    }
}

private fun magicSelectorFunction(probability: Double): Int {
    var temp = 2 / probability
    temp *= temp
    return temp.roundToInt()
}