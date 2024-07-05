package org.example

import java.lang.reflect.Array
import java.lang.reflect.Field
import java.util.*

object CopyUtils {

    @Throws(IllegalAccessException::class, InstantiationException::class)
    fun <T : Any> deepCopy(original: T?): T? {
        val copies = IdentityHashMap<Any, Any>()
        return deepCopy(original, copies)
    }

    @Throws(IllegalAccessException::class, InstantiationException::class)
    private fun <T : Any> deepCopy(original: T?, copies: MutableMap<Any, Any>): T? {
        if (original == null) {
            return null
        }

        if (copies.containsKey(original)) {
            return copies[original] as T
        }

        val clazz = original::class.java

        if (clazz.isPrimitive || original is String || original is Number || original is Boolean || original is Char) {
            return original
        }

        return when {
            clazz.isArray -> {
                val length = Array.getLength(original)
                val copy = Array.newInstance(clazz.componentType, length)
                copies[original] = copy

                for (i in 0 until length) {
                    Array.set(copy, i, deepCopy(Array.get(original, i), copies))
                }
                copy as T
            }

            original is Collection<*> -> {
                val copyCollection: MutableCollection<Any?> = if (original is List<*>) ArrayList() else HashSet()
                copies[original] = copyCollection

                for (item in original) {
                    copyCollection.add(deepCopy(item, copies))
                }

                copyCollection as T
            }

            original is Map<*, *> -> {
                val copyMap: MutableMap<Any?, Any?> = if (original is SortedMap<*, *>) TreeMap() else HashMap()
                copies[original] = copyMap

                for ((key, value) in original) {
                    copyMap[deepCopy(key, copies)] = deepCopy(value, copies)
                }

                copyMap as T
            }

            else -> {
                val copy = createInstance(clazz)
                copies[original] = copy

                for (field in getAllFields(clazz)) {
                    field.isAccessible = true
                    val fieldValue = field[original]
                    field[copy] = deepCopy(fieldValue, copies)
                }

                copy
            }
        }


    }

    private fun getAllFields(clazz: Class<*>): List<Field> {
        val fields = mutableListOf<Field>()
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            fields.addAll(currentClass.declaredFields)
            currentClass = currentClass.superclass
        }
        return fields
    }

    private fun <T : Any> createInstance(clazz: Class<T>): T {
        return clazz.getDeclaredConstructor().newInstance()
    }
}

data class ExampleObject(
    var id: Int = 0,
    var name: String? = null,
    var nestedObject: NestedObject? = null,
    var list: List<String>? = null,
    var set: Set<String>? = null,
    var map: Map<String, String>? = null,
    var array: IntArray? = null,
    var treeMap: TreeMap<String, String>? = null
)

data class NestedObject(
    var value: String? = null
)