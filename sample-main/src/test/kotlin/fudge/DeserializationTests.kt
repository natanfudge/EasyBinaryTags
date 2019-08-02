package fudge

import BadlyNamedSerializer
import NestedSerializer
import PrimitivesSerializer
import org.junit.jupiter.api.Test
import toTag
import java.util.*
import kotlin.test.assertEquals


class DeserializationTests {
    @Test
    fun testPrimitives() {
        val byteArray = byteArrayOf(50, 60)
        val intArray = intArrayOf(20, 30)
        val longArray = longArrayOf(10, 11)
        val primitives = createPrimitives(byteArray, intArray, longArray)
        val serialized = primitives.toTag()
        val deserialized = PrimitivesSerializer.fromTag(serialized)
        assertEquals(primitives, deserialized)

    }

    private fun createPrimitives(byteArray: ByteArray, intArray: IntArray, longArray: LongArray) = Primitives(
            name = "a", age = 12, length = 4, timesBreathed = 1, distanceToMars = 2, height = 2.5f, weight = 3.5,
            isMale = true, parentAges = byteArray, parentsTimeBreathed = intArray,
            parentsDistanceToMars = longArray, id = UUID(1, 10)
    )

    @Test
    fun testNested() {
        val byteArray = byteArrayOf(50, 60)
        val intArray = intArrayOf(20, 30)
        val longArray = longArrayOf(10, 11)
        val primitives = createPrimitives(byteArray, intArray, longArray)
        val nested = Nested(primitives, 15, PrimitivesShort(name = "amar", age = 69))
        val serialized = nested.toTag()
        val deserialized = NestedSerializer.fromTag(serialized)
        assertEquals(nested, deserialized)
    }

    @Test
    fun testBadlyNamed() {
        val badlyNamed = BadlyNamed(bool = false, isBadlyNamed = "badname", getBadlyNamed = 13, getBadBool = true)
        val serialized = badlyNamed.toTag()
        val deserialized = BadlyNamedSerializer.fromTag(serialized)
        assertEquals(badlyNamed,deserialized)
    }
}

