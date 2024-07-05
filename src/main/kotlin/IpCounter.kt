package org.example

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.zip.CRC32
import kotlin.concurrent.thread
import kotlin.math.ln

object IpCounter {

    fun countUniqueAddresses(filePath: String, numberOfThreads: Int, linesPerChunk: Int): Long {
        val bitSetSize = 1 shl 30
        val bitSet = ConcurrentBitSet(bitSetSize)
        val queue: BlockingQueue<List<String>> = LinkedBlockingQueue()

        val producerThread = thread {
            try {
                BufferedReader(FileReader(filePath)).use { reader ->
                    var line: String?
                    var lines = mutableListOf<String>()
                    while (reader.readLine().also { line = it } != null) {
                        lines.add(line!!)
                        if (lines.size >= linesPerChunk) {
                            queue.put(lines)
                            lines = mutableListOf()
                        }
                    }
                    if (lines.isNotEmpty()) {
                        queue.put(lines)
                    }
                }
            } catch (e: IOException) {
                println("Error reading the file: ${e.message}")
            } finally {
                repeat(numberOfThreads) {
                    queue.put(emptyList())
                }
            }
        }

        val threads = (0 until numberOfThreads).map {
            thread {
                while (true) {
                    val lines = queue.take()
                    if (lines.isEmpty()) break
                    processLines(lines, bitSet, bitSetSize)
                }
            }
        }

        producerThread.join()
        threads.forEach { it.join() }

        val zeroCount = bitSetSize - bitSet.cardinality()
        return linearCountingEstimate(zeroCount, bitSetSize)
    }

    fun processLines(lines: List<String>, bitSet: ConcurrentBitSet, bitSetSize: Int) {
        val crc32 = CRC32()
        for (line in lines) {
            crc32.reset()
            crc32.update(line.toByteArray())
            val hashValue = (crc32.value and 0xFFFFFFFFL).toInt() and (bitSetSize - 1)
            bitSet.set(hashValue)
        }
    }

    private fun linearCountingEstimate(zeroCount: Int, bitSetSize: Int): Long {
        return (bitSetSize * ln(bitSetSize.toDouble() / zeroCount)).toLong()
    }
}

class ConcurrentBitSet(size: Int) {
    private val bits = LongArray((size + 63) / 64)
    private val locks = Array((size + 63) / 64) { Any() }

    fun set(index: Int) {
        val arrayIndex = index ushr 6
        synchronized(locks[arrayIndex]) {
            bits[arrayIndex] = bits[arrayIndex] or (1L shl index)
        }
    }

    fun cardinality(): Int {
        var count = 0
        for (bit in bits) {
            count += java.lang.Long.bitCount(bit)
        }
        return count
    }
}