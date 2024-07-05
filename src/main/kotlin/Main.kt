package org.example

import java.util.*
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main() {
    printExecutionTime("testDeepCopy") {
        testDeepCopy()
    }

    printExecutionTime("testIpCounter") {
        testIpCounter()
    }
}

fun printExecutionTime(functionName: String, block: () -> Unit) {
    val executionTime = measureTimeMillis { block() }.toDuration(DurationUnit.MILLISECONDS)
    val minutes = executionTime.inWholeMinutes
    val seconds = executionTime.minus(minutes.toDuration(DurationUnit.MINUTES)).inWholeSeconds
    println("$functionName took $minutes m and $seconds s")
}

private fun testIpCounter() {
    val filePath = "ip_addresses/ip_addresses"
    val uniqueCount = IpCounter.countUniqueAddresses(filePath, 12, 10_000)

    println("Number of unique IP addresses: %,d.".format(uniqueCount))
}

private fun testDeepCopy() {
    try {
        val obj = ExampleObject(
            id = 1,
            name = null,
            nestedObject = NestedObject(value = "Nested Value"),
            list = arrayListOf("List Item"),
            set = setOf("Set Item"),
            map = hashMapOf("key" to "value"),
            array = intArrayOf(1, 2, 3),
            treeMap = TreeMap(mapOf("key" to "value")),
        )

        val copy = CopyUtils.deepCopy(obj)

        println("Original: $obj")
        println("Copy: $copy")

    } catch (e: Exception) {
        e.printStackTrace()
    }
}