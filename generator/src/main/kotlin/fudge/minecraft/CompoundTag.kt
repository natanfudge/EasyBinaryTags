package fudge.minecraft

import java.util.*

interface Tag
class CompoundTag : Tag {
    private val map = mutableMapOf<String, Any?>()
    fun putInt(key: String, int: Int) = map.put(key, int)
    fun putString(key: String, string: String) = map.put(key, string)
    fun putLong(key: String, long: Long) = map.put(key, long)
    fun put(key: String, tag: CompoundTag) = map.put(key, tag)
    fun putByte(key: String, byte: Byte) = map.put(key, byte)
    fun putShort(key: String, short: Short) = map.put(key, short)
    fun putFloat(key: String, float: Float) = map.put(key, float)
    fun putDouble(key: String, double: Double) = map.put(key, double)
    fun putBoolean(key: String, boolean: Boolean) = map.put(key, boolean)
    fun putByteArray(key: String, byteArray: ByteArray) = map.put(key, byteArray)
    fun putIntArray(key: String, intArray: IntArray) = map.put(key, intArray)
    fun putLongArray(key: String, longArray: LongArray) = map.put(key, longArray)
    fun putUuid(key: String, uuid: UUID) = map.put(key, uuid)


    fun getInt(key: String) = map[key] as Int
    fun getString(key: String) = map[key] as String
    fun getLong(key: String) = map[key] as Long
    fun getTag(key: String) = map[key] as Tag
    fun getByte(key: String) = map[key] as Byte
    fun getShort(key: String) = map[key] as Short
    fun getFloat(key: String) = map[key] as Float
    fun getDouble(key: String) = map[key] as Double
    fun getBoolean(key: String) = map[key] as Boolean
    fun getByteArray(key: String) = map[key] as ByteArray
    fun getIntArray(key: String) = map[key] as IntArray
    fun getLongArray(key: String) = map[key] as LongArray
    fun getUuid(key: String) = map[key] as UUID


    override fun toString() = "CompoundTag{ $map }"
}


/**
 * Puts the [BlockPos]'s information with the specified [key].
 * Retrieve the [BlockPos] from a [CompoundTag] by using [getBlockPos] with the same [key].
 */
fun CompoundTag.putBlockPos(key: String, pos: BlockPos?) {
    if (pos == null) return
    put(key, CompoundTag().apply {
        putInt("x", pos.x)
        putInt("y", pos.y)
        putInt("z", pos.z)
    })
    //TODO: change to asLong

}

/**
 * Retrieves the BlockPos from a [CompoundTag] with the specified [key], provided it was put there with [putBlockPos].
 *
 * Returns null if there is no [BlockPos] with the specified [key] in the [CompoundTag] (or if null was inserted).
 */
fun CompoundTag.getBlockPos(key: String): BlockPos? = getTag(key).run {
    BlockPos(getInt("x"), getInt("y"), getInt("z"))
}