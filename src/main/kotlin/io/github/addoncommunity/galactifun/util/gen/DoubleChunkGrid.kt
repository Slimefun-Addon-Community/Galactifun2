package io.github.addoncommunity.galactifun.util.gen

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * A grid of double values, stored in chunks.
 *
 * @param maxAccesses The maximum amount a chunk can be accessed before it is unloaded. Defaults to 256.
 */
class DoubleChunkGrid(private val maxAccesses: Int = 16 * 16) {

    private val chunks = Long2ObjectOpenHashMap<Array<DoubleArray>>()

    private val chunkAccesses = ConcurrentHashMap<Long, LongAdder>()

    private val lock = ReentrantReadWriteLock()

    operator fun get(cx: Int, cz: Int, x: Int, y: Int): Double {
        val id = cx pack cz

        lock.readLock().lock()
        val chunk = checkNotNull(chunks[id]) { "Chunk at $cx, $cz not generated" }
        val stack = chunk[x][y]
        lock.readLock().unlock()

        check(!stack.isNaN()) { "Stack at $x, $y in chunk at $cx, $cz not generated" }
        increment(id)
        return stack
    }

    operator fun get(x: Int, y: Int) = get(x shr 4, y shr 4, x and 15, y and 15)

    operator fun set(cx: Int, cz: Int, x: Int, y: Int, value: Double) {
        val id = cx pack cz

        lock.writeLock().lock()
        val chunk = chunks.computeIfAbsent(id, Long2ObjectFunction { Array(16) { DoubleArray(16) { Double.NaN } } })
        chunk[x][y] = value
        lock.writeLock().unlock()
    }

    operator fun set(x: Int, y: Int, value: Double) = set(x shr 4, y shr 4, x and 15, y and 15, value)

    fun getOrSet(cx: Int, cz: Int, x: Int, y: Int, value: () -> Double): Double {
        val id = cx pack cz

        lock.readLock().lock()
        val chunk = chunks[id]
        if (chunk != null) {
            val stack = chunk[x][y]
            if (!stack.isNaN()) {
                lock.readLock().unlock()
                increment(id)
                return stack
            }
        }
        lock.readLock().unlock()

        increment(id)

        val computed = value()
        this[cx, cz, x, y] = computed
        return computed
    }

    fun getOrSet(x: Int, y: Int, value: () -> Double) =
        getOrSet(x shr 4, y shr 4, x and 15, y and 15, value)

    private fun increment(id: Long) {
        chunkAccesses.computeIfAbsent(id) { _ -> LongAdder() }.increment()
        val accesses = chunkAccesses[id] ?: return // in case another thread removes it
        if (accesses.sum() >= maxAccesses) {
            lock.writeLock().lock()
            chunkAccesses.remove(id)
            chunks.remove(id)
            lock.writeLock().unlock()
        }
    }
}

private infix fun Int.pack(other: Int) = this.toLong() shl 32 or other.toLong()