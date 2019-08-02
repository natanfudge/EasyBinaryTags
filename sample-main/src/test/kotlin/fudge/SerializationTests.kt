package fudge

import fudge.minecraft.CompoundTag
import org.junit.jupiter.api.Test
import toTag
import java.util.*
import kotlin.test.assertEquals



@NbtSerializable
data class Primitives(val name: String,
                      val age: Byte,
                      val length: Short,
                      val timesBreathed: Int,
                      val distanceToMars: Long,
                      val height: Float,
                      val weight: Double,
                      val isMale: Boolean,
                      val parentAges: ByteArray,
                      val parentsTimeBreathed: IntArray,
                      val parentsDistanceToMars: LongArray,
                      val id: UUID
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Primitives

        if (name != other.name) return false
        if (age != other.age) return false
        if (length != other.length) return false
        if (timesBreathed != other.timesBreathed) return false
        if (distanceToMars != other.distanceToMars) return false
        if (height != other.height) return false
        if (weight != other.weight) return false
        if (isMale != other.isMale) return false
        if (!parentAges.contentEquals(other.parentAges)) return false
        if (!parentsTimeBreathed.contentEquals(other.parentsTimeBreathed)) return false
        if (!parentsDistanceToMars.contentEquals(other.parentsDistanceToMars)) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + age
        result = 31 * result + length
        result = 31 * result + timesBreathed
        result = 31 * result + distanceToMars.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + isMale.hashCode()
        result = 31 * result + parentAges.contentHashCode()
        result = 31 * result + parentsTimeBreathed.contentHashCode()
        result = 31 * result + parentsDistanceToMars.contentHashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}

@NbtSerializable
data class PrimitivesShort(val name: String, val age: Int)

@NbtSerializable
data class Nested(val obj: Primitives, val primitive: Int, val anotherObj: PrimitivesShort) {
    private val x: Int = 0
    protected val y: Double = 0.0
}

@NbtSerializable
data class BadlyNamed(val bool: Boolean, val isBadlyNamed :String, val getBadlyNamed : Int,val getBadBool : Boolean)

class SerializationTests {
    @Test
    fun testPrimitives() {
        val byteArray = byteArrayOf(50, 60)
        val intArray = intArrayOf(20, 30)
        val longArray = longArrayOf(10, 11)
        val primitives = createPrimitives(byteArray, intArray, longArray)
        val serialized = primitives.toTag()
        verifySerializedPrimitives(serialized, byteArray, intArray, longArray)

    }

    private fun verifySerializedPrimitives(serialized: CompoundTag, byteArray: ByteArray, intArray: IntArray, longArray: LongArray) {
        assertEquals("a", serialized.getString("name"))
        assertEquals(12, serialized.getByte("age"))
        assertEquals(4, serialized.getShort("length"))
        assertEquals(1, serialized.getInt("timesBreathed"))
        assertEquals(2, serialized.getLong("distanceToMars"))
        assertEquals(2.5f, serialized.getFloat("height"))
        assertEquals(3.5, serialized.getDouble("weight"))
        assertEquals(true, serialized.getBoolean("isMale"))
        assertEquals(byteArray, serialized.getByteArray("parentAges"))
        assertEquals(intArray, serialized.getIntArray("parentsTimeBreathed"))
        assertEquals(longArray, serialized.getLongArray("parentsDistanceToMars"))
        assertEquals(UUID(1, 10), serialized.getUuid("id"))
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
        val primitivesSerialized = serialized.getTag("obj") as CompoundTag
        val shortPrimitivesSerialized = serialized.getTag("anotherObj") as CompoundTag
        assertEquals(15, serialized.getInt("primitive"))
        verifySerializedPrimitives(primitivesSerialized, byteArray, intArray, longArray)
        assertEquals("amar", shortPrimitivesSerialized.getString("name"))
        assertEquals(69, shortPrimitivesSerialized.getInt("age"))
    }

    @Test
    fun testBadlyNamed(){
        val badlyNamed = BadlyNamed(bool = false, isBadlyNamed = "badname", getBadlyNamed = 13, getBadBool = true)
        val serialized = badlyNamed.toTag()
        assertEquals(false, serialized.getBoolean("bool"))
        assertEquals("badname", serialized.getString("isBadlyNamed"))
        assertEquals(13, serialized.getInt("getBadlyNamed"))
        assertEquals(true, serialized.getBoolean("getBadBool"))
    }
}
