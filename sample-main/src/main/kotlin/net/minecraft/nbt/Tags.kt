package net.minecraft.nbt

import java.util.*
import kotlin.collections.AbstractList

interface Tag

class CompoundTag : Tag {
    private val map = mutableMapOf<String, Any?>()
//    fun putInt(key: String, int: Int) = map.put(key, int)
//    fun putString(key: String, string: String) = map.put(key, string)
//    fun putLong(key: String, long: Long) = map.put(key, long)
    fun put(key: String, tag: Tag) = map.put(key, tag)
//    fun putByte(key: String, byte: Byte) = map.put(key, byte)
//    fun putShort(key: String, short: Short) = map.put(key, short)
//    fun putFloat(key: String, float: Float) = map.put(key, float)
//    fun putDouble(key: String, double: Double) = map.put(key, double)
//    fun putBoolean(key: String, boolean: Boolean) = map.put(key, boolean)
//    fun putByteArray(key: String, byteArray: ByteArray) = map.put(key, byteArray)
//    fun putIntArray(key: String, intArray: IntArray) = map.put(key, intArray)
//    fun putLongArray(key: String, longArray: LongArray) = map.put(key, longArray)
//    fun putUuid(key: String, uuid: UUID) = map.put(key, uuid)


//    fun getInt(key: String) = (map[key] as IntTag).getInt()
//    fun getString(key: String) = (map[key] as StringTag).getString()
//    fun getLong(key: String) = (map[key] as LongTag).getLong()
//    fun getByte(key: String) = (map[key] as ByteTag).getByte()
//    fun getShort(key: String) = (map[key] as ShortTag).getShort()
//    fun getFloat(key: String) = (map[key] as FloatTag).getFloat()
//    fun getDouble(key: String) = (map[key] as DoubleTag).getDouble()
//    fun getBoolean(key: String) = (map[key] as ByteTag).getByte() != 0.toByte()
//    fun getByteArray(key: String) = (map[key] as ByteArrayTag).getByteArray()
//    fun getIntArray(key: String) = (map[key] as IntArrayTag).getIntArray()
//    fun getLongArray(key: String) = (map[key] as LongArrayTag).getLongArray()
    fun getTag(key: String) = map[key] as Tag
//    fun getUuid(key: String) = UUID(map[key + "Most"] as Long, map[key + "Least"] as Long)


    override fun toString() = "CompoundTag{ $map }"
}

fun foo(){
    val x :java.util.AbstractList<String> = arrayListOf()
//    x.ad
    val y = x.map { it + "1" }
}

class ListTag(private val list : MutableList<Tag> = mutableListOf()) : Tag , MutableList<Tag> by list{
//    private val list = mutableListOf<Any>()

//    fun getCompoundTag(pos: Int) = list[pos] as CompoundTag
//    fun getListTag(pos: Int) = list[pos] as ListTag
//    fun getShort(pos: Int) = list[pos] as Short
//    fun getInt(pos: Int) = list[pos] as Int
//    fun getIntArray(pos: Int) = list[pos] as IntArray
//    fun getFloat(pos: Int) = list[pos] as Float
//    fun getString(pos: Int) = list[pos] as String
//    fun getDouble(pos: Int) = list[pos] as Double
//    fun addTag(tag: Tag) = list.add(tag)
//    fun setTag(pos: Int, tag: Tag) = list.set(pos, tag)
}

class ByteArrayTag(private val value: ByteArray) : Tag {
    fun getByteArray() = value
}

class ByteTag(private val value: Byte) : Tag {
    fun getByte() = value
}

class LongArrayTag(private val value: LongArray) : Tag {
    fun getLongArray() = value
}

class LongTag(private val value: Long) : Tag {
    fun getLong() = value
}

class ShortTag(private val value: Short) : Tag {
    fun getShort() = value
}

class IntTag(private val value: Int) : Tag {
    fun getInt() = value
}

class IntArrayTag(private val value: IntArray) : Tag {
    fun getIntArray() = value
}

class FloatTag(private val value: Float) : Tag {
    fun getFloat() = value
}

class StringTag(private val value: String) : Tag {
    fun getString() = value
}

class DoubleTag(private val value: Double) : Tag {
    fun getDouble() = value
}

