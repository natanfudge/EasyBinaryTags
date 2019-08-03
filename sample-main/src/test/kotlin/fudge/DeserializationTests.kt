package fudge

import BadlyNamedSerializer
import ListsSerializer
import NestedSerializer
import PrimitivesSerializer
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import toTag
import java.util.*
import kotlin.test.assertEquals

//TODO: tests null values
//TODO: run these tests in a modded environment
@NbtSerializable
data class Lists(val stringList: List<String>, val primitivesList: List<PrimitivesShort>,
                 val listList: List<List<List<PrimitivesShort>>>, val uuids: List<UUID>, val uuidsListList: List<List<UUID>>
                 , val emptyList: List<String>
)

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
                      val id: UUID,
                      val blockPos: BlockPos
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
        if (blockPos != other.blockPos) return false

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
        result = 31 * result + blockPos.hashCode()
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
data class BadlyNamed(val bool: Boolean, val isBadlyNamed: String, val getBadlyNamed: Int, val getBadBool: Boolean)

fun createPrimitives() = Primitives(
        name = "a", age = 12, length = 4, timesBreathed = 1, distanceToMars = 2, height = 2.5f, weight = 3.5,
        isMale = true, parentAges = byteArrayOf(50, 60), parentsTimeBreathed = intArrayOf(20, 30),
        parentsDistanceToMars = longArrayOf(10, 11), id = UUID(1, 10),
        blockPos = BlockPos(1, 2, 3)
)

class DeserializationTests {
    @Test
    fun testPrimitives() {
        val primitives = createPrimitives()
        val serialized = primitives.toTag()
        val deserialized = PrimitivesSerializer.fromTag(serialized)
        assertEquals(primitives, deserialized)
    }

    //TODO: test null values


    @Test
    fun testNested() {
        val primitives = createPrimitives()
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
        assertEquals(badlyNamed, deserialized)
    }

    @Test
    fun testLists() {
        val stringList = listOf("hello", "garls", "and", "girls")
        val listList = listOf(
                listOf(
                        listOf(PrimitivesShort("den", 2), PrimitivesShort("ben", 3)),
                        listOf(PrimitivesShort("a", 12), PrimitivesShort("asdf", 11))
                ),
                listOf(
                        listOf(PrimitivesShort("daen", 32), PrimitivesShort("ben", 3)),
                        listOf(PrimitivesShort("a2", 12), PrimitivesShort("asdf", 111))
                )
        )
        val primitivesList = listOf(PrimitivesShort("den", 2), PrimitivesShort("ben", 3))
        val uuids = listOf(UUID(1242351, 12341234), UUID(22, 1231), UUID(4512, 1))
        val emptyList = listOf<String>()
        val uuidsListList = listOf(
                listOf(UUID(1242351, 12341234), UUID(22, 1231), UUID(4512, 1)),
                listOf(UUID(12412351, 2234), UUID(22, 1231), UUID(4512, 1))
        )
        val lists = Lists(
                stringList = stringList,
                listList = listList,
                primitivesList = primitivesList,
                uuids = uuids, emptyList = emptyList,
                uuidsListList = uuidsListList
        )
        val serialized = lists.toTag()
        val deserialized = ListsSerializer.fromTag(serialized)
        assertEquals(lists.stringList, deserialized.stringList)
        assertEquals(lists.listList, deserialized.listList)
        assertEquals(lists.primitivesList, deserialized.primitivesList)
        assertEquals(lists.uuids, deserialized.uuids)
        assertEquals(uuidsListList.size,deserialized.uuidsListList.size)
        lists.uuidsListList.forEachIndexed { i, list ->
            assertEquals(list.size,deserialized.uuidsListList[i].size, "pos = $i")
            list.forEachIndexed { j, uuid ->
                assertEquals(uuid,deserialized.uuidsListList[i][j], "pos = $i,$j")
            }
        }
//        assertEquals(lists.uuidsListList, deserialized.uuidsListList)

    }
}


//fun Lists.toTag(): CompoundTag = CompoundTag().apply {
//    put("stringList", ListTag().apply {
//        for (element1 in stringList) {
//            add(StringTag(element1))
//        }
//    })
//    put("primitivesList", ListTag().apply {
//        for (element1 in primitivesList) {
//            add(element1.toTag())
//        }
//    })
//    put("listList", ListTag().apply {
//        for (element1 in listList) {
//            add(ListTag().apply {
//                for (element2 in element1) {
//                    add(ListTag().apply {
//                        for (element3 in element2) {
//                            add(element3.toTag())
//                        }
//                    })
//                }
//            })
//        }
//    })
//
//    put("uuids", ListTag().apply {
//        for(element1 in uuids){
////            add(element1)
//        }
//    })
//}
//
//fun fromTag(tag: CompoundTag): Lists = tag.run {
//    Lists((tag.getTag("stringList") as ListTag).let {
//        mutableListOf<String>().apply {
//            it.forEachIndexed { index, tag ->
//                add(index, (tag as StringTag).getString())
//            }
//        }
//
//    },
//            (tag.getTag("primitivesList") as ListTag).map { element1 -> PrimitivesShortSerializer.fromTag(element1 as CompoundTag) },
//            (tag.getTag("listList") as ListTag).map { element1 ->
//                (element1 as ListTag).map { element2 ->
//                    (element2 as ListTag).map { element3 ->
//                        PrimitivesShortSerializer.fromTag(element3 as CompoundTag)
//                    }
//                }
//            },
//
//
//            (tag.getTag("uuidList") as ListTag).let {
//                var mostSig: Long = 0
//
//                mutableListOf<UUID>().apply {
//                    it.forEachIndexed { index, tag ->
//                        if (index % 2 == 0) mostSig = (tag as LongTag).getLong()
//                        else {
//                            add(UUID(mostSig, (tag as LongTag).getLong()))
//                        }
//                    }
//                }
//
//            }
//
//    )
//}