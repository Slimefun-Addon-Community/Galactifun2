package io.github.addoncommunity.galactifun.util.gen

import com.google.common.util.concurrent.AtomicDoubleArray
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * A grid of double values, stored in chunks.
 *
 * */
class DoubleChunkGrid {

    private val chunks = Long2ObjectOpenHashMap<Array<AtomicDoubleArray>>()

    private val chunkCreations = Long2LongOpenHashMap()

    private val lock = ReentrantReadWriteLock()

    init {
        val ref = WeakReference(this)
        coroScope.launch {
            while (true) {
                val time = System.currentTimeMillis()
                val grid = ref.get() ?: break
                grid.lock.writeLock().lock()
                val iterator = grid.chunkCreations.long2LongEntrySet().fastIterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (time - entry.longValue > 3000) {
                        iterator.remove()
                        grid.chunks.remove(entry.longKey)
                    }
                }
                grid.lock.writeLock().unlock()
                delay(2999)
            }
        }
    }

    operator fun get(cx: Int, cz: Int, x: Int, y: Int): Double {
        val id = cx pack cz

        lock.readLock().lock()
        val chunk = checkNotNull(chunks[id]) { "Chunk at $cx, $cz not generated" }
        lock.readLock().unlock()
        val stack = chunk[x][y]

        check(!stack.isNaN()) { "Stack at $x, $y in chunk at $cx, $cz not generated" }
        return stack
    }

    operator fun get(x: Int, y: Int) = get(x shr 4, y shr 4, x and 15, y and 15)

    operator fun set(cx: Int, cz: Int, x: Int, y: Int, value: Double) {
        val id = cx pack cz

        val chunk = if (chunks.containsKey(id)) {
            chunks[id]!!
        } else {
            val chunk = Array(16) { AtomicDoubleArray(DoubleArray(16) { Double.NaN }) }
            lock.writeLock().lock()
            chunks[id] = chunk
            chunkCreations[id] = System.currentTimeMillis()
            lock.writeLock().unlock()
            chunk
        }
        chunk[x][y] = value
    }

    operator fun set(x: Int, y: Int, value: Double) = set(x shr 4, y shr 4, x and 15, y and 15, value)

    fun getOrSet(cx: Int, cz: Int, x: Int, y: Int, value: () -> Double): Double {
        val id = cx pack cz

        lock.readLock().lock()
        val chunk = chunks[id]
        lock.readLock().unlock()
        if (chunk != null) {
            val stack = chunk[x][y]
            if (!stack.isNaN()) {
                return stack
            }
        }

        val computed = value()
        this[cx, cz, x, y] = computed
        return computed
    }

    fun getOrSet(x: Int, y: Int, value: () -> Double) =
        getOrSet(x shr 4, y shr 4, x and 15, y and 15, value)

    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val coroScope = CoroutineScope(newFixedThreadPoolContext(4, "Galactifun2-DoubleChunkGrid"))
    }
}

private infix fun Int.pack(other: Int) = (this.toLong() shl 32) or (other.toLong() and 0xFFFFFFFFL)