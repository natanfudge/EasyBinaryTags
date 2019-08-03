//package fudge.serialization
//
//import fudge.minecraft.BlockPos
//import fudge.minecraft.CompoundTag
//import fudge.minecraft.getBlockPos
//import fudge.minecraft.putBlockPos
//import kotlin.reflect.KProperty1
//
//interface SerializableBase {
//    fun addToTag(tag: CompoundTag, key: String)
//}
//
//abstract class Serializable(private vararg val serials: SerializableBase) : SerializableBase {
//    fun toTag(): CompoundTag = CompoundTag().apply {
//        serials.forEachIndexed { index, serializable ->
//            serializable.addToTag(this, index.toString())
//        }
//    }
//
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.put(key, toTag())
//    }
//}
//
//
//class SerializableInt(private val int: Int) : SerializableBase {
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.putInt(key, int)
//    }
//}
//
//val Int.sr get() = SerializableInt(this)
//
//class SerializableString(private val string: String) : SerializableBase {
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.putString(key, string)
//    }
//}
//
//val String.sr get() = SerializableString(this)
//
//class SerializableBlockPos(private val blockPos: BlockPos) : SerializableBase {
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.putBlockPos(key, blockPos)
//    }
//}
//
//val BlockPos.sr get() = SerializableBlockPos(this)
//
//
//fun <T> DeserializationContext.convertPropertyToObject(property: KProperty1<T, *>): Any {
//    @Suppress("UNCHECKED_CAST")
//    property as KProperty1<Any, *>
//    val jClass = property.javaClass
//
//    return when {
//        jClass.isAssignableFrom(String::class.java) -> string
//        jClass.isAssignableFrom(BlockPos::class.java) -> blockPos
//        jClass.isAssignableFrom(int::class.java) -> int
//        else -> TODO()
//    }
//}
//
//
//inline fun <T> CompoundTag.deserialize(deserializer: DeserializationContext.() -> T): T =
//        DeserializationContext(this).deserializer()
//
//class DeserializationContext(private val tag: CompoundTag) {
//    private var count: Int = 0
//
//    private fun nextString() = count++.toString()
//
//    val string get() : String = tag.getString(nextString())
//    val blockPos get() : BlockPos = tag.getBlockPos(nextString())!!
//    val int get(): Int = tag.getInt(nextString())
//
//    fun <T> custom(deserializer: Deserializer<T>): T = deserializer.fromTag(tag.getTag(nextString()) as CompoundTag)
//
//}
//
//
//interface Deserializer<T> {
//    fun fromTag(tag: CompoundTag): T
//}
//
//data class MyClass(val name: String, val pos: BlockPos, val age: Int) : Serializable(name.sr, pos.sr, age.sr) {
//    companion object : Deserializer<MyClass> {
//        override fun fromTag(tag: CompoundTag) = tag.deserialize {
//            MyClass(string, blockPos, int)
//        }
//
//    }
//}
//
//data class NestedClass(val clazz: MyClass) : Serializable(clazz) {
//    companion object {
//        fun fromTag(tag: CompoundTag) = tag.deserialize {
//            NestedClass(custom(MyClass))
//        }
//    }
//}