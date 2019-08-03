//package fudge
//
//import net.minecraft.nbt.CompoundTag
//import net.minecraft.util.math.BlockPos
//import org.junit.jupiter.api.Test
//import toTag
//import java.util.*
//import kotlin.test.assertEquals
//
//
//
//@NbtSerializable
//data class Primitives(val name: String,
//                      val age: Byte,
//                      val length: Short,
//                      val timesBreathed: Int,
//                      val distanceToMars: Long,
//                      val height: Float,
//                      val weight: Double,
//                      val isMale: Boolean,
//                      val parentAges: ByteArray,
//                      val parentsTimeBreathed: IntArray,
//                      val parentsDistanceToMars: LongArray,
//                      val id: UUID,
//                      val blockPos:BlockPos
//) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Primitives
//
//        if (name != other.name) return false
//        if (age != other.age) return false
//        if (length != other.length) return false
//        if (timesBreathed != other.timesBreathed) return false
//        if (distanceToMars != other.distanceToMars) return false
//        if (height != other.height) return false
//        if (weight != other.weight) return false
//        if (isMale != other.isMale) return false
//        if (!parentAges.contentEquals(other.parentAges)) return false
//        if (!parentsTimeBreathed.contentEquals(other.parentsTimeBreathed)) return false
//        if (!parentsDistanceToMars.contentEquals(other.parentsDistanceToMars)) return false
//        if (id != other.id) return false
//        if (blockPos != other.blockPos) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = name.hashCode()
//        result = 31 * result + age
//        result = 31 * result + length
//        result = 31 * result + timesBreathed
//        result = 31 * result + distanceToMars.hashCode()
//        result = 31 * result + height.hashCode()
//        result = 31 * result + weight.hashCode()
//        result = 31 * result + isMale.hashCode()
//        result = 31 * result + parentAges.contentHashCode()
//        result = 31 * result + parentsTimeBreathed.contentHashCode()
//        result = 31 * result + parentsDistanceToMars.contentHashCode()
//        result = 31 * result + id.hashCode()
//        result = 31 * result + blockPos.hashCode()
//        return result
//    }
//
//}
//
//@NbtSerializable
//data class PrimitivesShort(val name: String, val age: Int)
//
//@NbtSerializable
//data class Nested(val obj: Primitives, val primitive: Int, val anotherObj: PrimitivesShort) {
//    private val x: Int = 0
//    protected val y: Double = 0.0
//}
//
//@NbtSerializable
//data class BadlyNamed(val bool: Boolean, val isBadlyNamed :String, val getBadlyNamed : Int,val getBadBool : Boolean)
//     fun createPrimitives() = Primitives(
//            name = "a", age = 12, length = 4, timesBreathed = 1, distanceToMars = 2, height = 2.5f, weight = 3.5,
//            isMale = true, parentAges = byteArrayOf(50, 60), parentsTimeBreathed = intArrayOf(20, 30),
//            parentsDistanceToMars = longArrayOf(10, 11), id = UUID(1, 10),
//            blockPos = BlockPos(1,2,3)
//    )
//
////@NbtSerializable
////data class Lists(val stringList: List<String>, val primitivesList: List<PrimitivesShort>, val listList: List<List<List<PrimitivesShort>>>)
//
//
//class SerializationTests {
//    @Test
//    fun testPrimitives() {
//        val primitives = createPrimitives()
//        val serialized = primitives.toTag()
//        verifySerializedPrimitives(serialized)
//
//    }
//
//    private fun verifySerializedPrimitives(serialized: CompoundTag) {
//        assertEquals("a", serialized.getString("name"))
//        assertEquals(12, serialized.getByte("age"))
//        assertEquals(4, serialized.getShort("length"))
//        assertEquals(1, serialized.getInt("timesBreathed"))
//        assertEquals(2, serialized.getLong("distanceToMars"))
//        assertEquals(2.5f, serialized.getFloat("height"))
//        assertEquals(3.5, serialized.getDouble("weight"))
//        assertEquals(true, serialized.getBoolean("isMale"))
//        assert(byteArrayOf(50, 60).contentEquals(serialized.getByteArray("parentAges")))
//        assert(intArrayOf(20, 30).contentEquals(serialized.getIntArray("parentsTimeBreathed")))
//        assert(longArrayOf(10, 11).contentEquals(serialized.getLongArray("parentsDistanceToMars")))
//        assertEquals(UUID(1, 10), serialized.getUuid("id"))
//        assertEquals(BlockPos(1,2,3), BlockPos.fromLong(serialized.getLong("blockPos")))
//    }
//
//
//
//    @Test
//    fun testNested() {
//        val primitives = createPrimitives()
//        val nested = Nested(primitives, 15, PrimitivesShort(name = "amar", age = 69))
//        val serialized = nested.toTag()
//        val primitivesSerialized = serialized.getTag("obj") as CompoundTag
//        val shortPrimitivesSerialized = serialized.getTag("anotherObj") as CompoundTag
//        assertEquals(15, serialized.getInt("primitive"))
//        verifySerializedPrimitives(primitivesSerialized)
//        assertEquals("amar", shortPrimitivesSerialized.getString("name"))
//        assertEquals(69, shortPrimitivesSerialized.getInt("age"))
//    }
//
//    @Test
//    fun testBadlyNamed(){
//        val badlyNamed = BadlyNamed(bool = false, isBadlyNamed = "badname", getBadlyNamed = 13, getBadBool = true)
//        val serialized = badlyNamed.toTag()
//        assertEquals(false, serialized.getBoolean("bool"))
//        assertEquals("badname", serialized.getString("isBadlyNamed"))
//        assertEquals(13, serialized.getInt("getBadlyNamed"))
//        assertEquals(true, serialized.getBoolean("getBadBool"))
//    }
//}
//
