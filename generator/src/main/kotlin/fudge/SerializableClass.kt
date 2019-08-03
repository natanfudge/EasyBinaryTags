package fudge

import com.sun.xml.internal.ws.developer.Serialization
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

data class SerializableClass(val fields: List<SerializableProperty>)

data class SerializableProperty(val name: String, val type: String) {

}

//data class PropertyConstructionInfo(val wrapperName : String?, val )

data class MethodSuffixAndArgsResult(val suffix: String, val propertyName: String, val simpleTypeName: String) {
    val isTag get() = suffix == ""
}

class SerializationMethod(val serialization: String,
        // Used specifically for UUID, that has 2 put statements
                          val serializationPart2: String? = null,
                          val deserialization: String)

private fun result(propertyName: String, type: String, suffix: String) = MethodSuffixAndArgsResult(suffix, propertyName, type.split(".").last())

//TODO: lists

fun String.simpleName() = split(".").last()

//fun args(name: String) = "(\"$name\", $name)"

///**
// * Returns int("age", age), ByteArray("parentAges",parentAges), etc
// * Returns  MethodSuffixAndArgsResult instead of a string so we can know later if need to add "tag" to "put" so we can have "putTag"
// */
//TODO: change this to put(key, new XTag(x)) so it works better with lists, but be careful because sometimes the implementation is different.
// Also no need for MethodSuffixAndArgsResult in that case I think

fun SerializableProperty.getSerializationMethod(serializables: Set<String>, className: String,
                                                processingEnv: ProcessingEnvironment, element: Element,
                                                nestingLevel: Int = 0, isForList: Boolean = false
): SerializationMethod {
    println("Processing $this")

    val serializationPrefix = if (isForList) "add(" else "put(\"$name\", "
    val deserializationTarget = if (isForList) "element$nestingLevel" else "getTag(\"$name\")"

    // Java types
    val typeWithoutJavaLang = type.removePrefix("java.lang.")
    if (typeWithoutJavaLang in javaPrimitives) {
        val suffix = typeWithoutJavaLang.toTitleCase()
        return primitiveSerializationMethod(suffix, serializationPrefix, deserializationTarget)
    }


    // Kotlin primitive types
    val typeWithoutKotlin = type.removePrefix("kotlin.")
    if (typeWithoutKotlin in kotlinPrimitives) {
        return primitiveSerializationMethod(typeWithoutKotlin, serializationPrefix, deserializationTarget)
    }
    // Boolean (special case)
    if (typeWithoutKotlin == "Boolean") {
        return SerializationMethod(
                serialization = "${serializationPrefix}if($name) net.minecraft.nbt.ByteTag(1) else net.minecraft.nbt.ByteTag(0))",
                deserialization = "($deserializationTarget as net.minecraft.nbt.ByteTag).getByte() != 0.toByte()"
        )
    }

    // Kotlin array types
    val typeWithoutArraySurrounding = type.removePrefix("kotlin.Array<kotlin.").removeSuffix(">")
    if (typeWithoutArraySurrounding in kotlinArrayTypes) {
        return primitiveSerializationMethod(typeWithoutArraySurrounding + "Array", serializationPrefix, deserializationTarget)
    }

    //UUID
    if (type == "java.util.UUID") {
        // UUID lists are handled separately
        return SerializationMethod(
                serialization = "put(\"$name\" + \"Most\", net.minecraft.nbt.LongTag($name.getMostSignificantBits()))\n" +
                        "put(\"$name\" + \"Least\", net.minecraft.nbt.LongTag($name.getLeastSignificantBits()))",
                deserialization = "java.util.UUID((getTag(\"$name\" + \"Most\") as net.minecraft.nbt.LongTag).getLong(), (getTag(\"$name\" + \"Least\") as net.minecraft.nbt.LongTag).getLong())"
        )
    }
    // BlockPos
    if (type == "net.minecraft.util.math.BlockPos") {
        return SerializationMethod(
                serialization = "${serializationPrefix}net.minecraft.nbt.LongTag($name.toLong()))",
                deserialization = "net.minecraft.util.math.BlockPos.fromLong(($deserializationTarget as net.minecraft.nbt.LongTag).getLong())"
        )
    }

    // Custom serializables
    if (type in serializables) {
        val deserializer = "${type.simpleName()}$DeserializerSuffix"
        return SerializationMethod(
                serialization = "${serializationPrefix}$name.toTag())",
                deserialization = "$deserializer.fromTag($deserializationTarget as CompoundTag)"
        )
    }

    if (type.startsWith("java.util.List")) {
        val genericType = type.removePrefix("java.util.List<").removeSuffix(">")
        return listSerializationMethod(
                genericType, serializables, className, processingEnv, element, nestingLevel + 1,serializationPrefix,
                deserializationTarget,isForList
        )
    }


    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Hello there." +
            " It appears you tried to put @NbtSerializable on a class named '$className' with a property that is not serializable named '$name'." +
            " Make sure every property is either: A Primitive, UUID, BlockPos, or your own class that is annotated with @NbtSerializable." +
            " ByteArray, IntArray and LongArray, and a list of any NbtSerializable also work out of the box.\n" +
            "If you are certain this property is serializable, please submit an issue on Github.\n" +
            "(Could not find type $type in existing serializables: $serializables)", element)

    return SerializationMethod("", "", "")
}

/**
 * [nestingLevel] is so we don't have multiple variables with the same name in the same closure.
 */
private fun SerializableProperty.listSerializationMethod(genericType: String, serializables: Set<String>,
                                                         className: String, processingEnv: ProcessingEnvironment,
                                                         element: Element, nestingLevel: Int,
                                                         serializationPrefix: String, deserializationTarget: String, isForList: Boolean): SerializationMethod {
    // UUID is handled separately because it's a fucking pain to generalize
    if (genericType == "java.util.UUID") {
        return SerializationMethod(
                serialization = """${serializationPrefix}net.minecraft.nbt.ListTag().apply { 
        for(element$nestingLevel in $name){
            add(net.minecraft.nbt.LongTag(element$nestingLevel.getMostSignificantBits()))
            add(net.minecraft.nbt.LongTag(element$nestingLevel.getLeastSignificantBits()))
        }
    })
                    
""",
                deserialization = """$deserializationTarget.let {
    it as net.minecraft.nbt.ListTag
    var mostSig: Long = 0

    mutableListOf<java.util.UUID>().apply {
        it.forEachIndexed { index, tag ->
            if (index.rem(2) == 0) mostSig = (tag as net.minecraft.nbt.LongTag).getLong()
            else {
                add(java.util.UUID(mostSig, (tag as net.minecraft.nbt.LongTag).getLong()))
            }
        }
    }
}"""
        )
    }
    else {
        val genericTypeSerializationMethod = SerializableProperty(name = "element$nestingLevel", type = genericType)
                .getSerializationMethod(serializables, className, processingEnv, element, nestingLevel, isForList = true)

        return SerializationMethod(
                serialization =
                """${serializationPrefix}net.minecraft.nbt.ListTag().apply {
    for(element$nestingLevel in $name){
        ${genericTypeSerializationMethod.serialization}
    }
})"""
                ,
                deserialization = "($deserializationTarget as net.minecraft.nbt.ListTag).map { element$nestingLevel -> ${genericTypeSerializationMethod.deserialization} }"
        )
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
//}
//
//fun fromTag(tag: CompoundTag): Lists = tag.run {
//    Lists((tag.getTag("stringList") as ListTag).map { element1 -> (element1 as StringTag).getString() },
//            (tag.getTag("primitivesList") as ListTag).map { element1 -> PrimitivesShortSerializer.fromTag(element1 as CompoundTag) },
//            (tag.getTag("listList") as ListTag).map { element1 ->
//                (element1 as ListTag).map { element2 ->
//                    (element2 as ListTag).map { element3 ->
//                        PrimitivesShortSerializer.fromTag(element3 as CompoundTag)
//                    }
//                }
//            }
//    )
//}


private fun SerializableProperty.primitiveSerializationMethod(primitiveName: String, serializationPrefix: String,
                                                              deserializedTarget: String): SerializationMethod {
    return SerializationMethod(
            serialization = "${serializationPrefix}net.minecraft.nbt.${primitiveName}Tag($name))",
            deserialization = "($deserializedTarget as net.minecraft.nbt.${primitiveName}Tag).get${primitiveName}()"
    )
}

fun String.toTitleCase() = this[0].toUpperCase() + this.substring(1, length)
fun String.toCamelCase() = this[0].toLowerCase() + this.substring(1, length)

class AnnotationProcessingException(string: String) : Exception(string)

val javaPrimitives = listOf("String"/*,"byte", "short", "float", "double", "boolean",  "int", "long"*/)
val kotlinPrimitives = listOf("Byte", "Short", "Float", "Double", /*"Boolean", */"String", "Int", "Long")
val kotlinArrayTypes = listOf("Byte", "Int", "Long")


//fun SerializableProperty.getSerializationMethod(serializables: Set<String>, className: String, processingEnv: ProcessingEnvironment, element: Element, nestingLevel: Int = 0): SerializationMethod {
//    println("Processing $this")
//    // Java types
//    val typeWithoutJavaLang = type.removePrefix("java.lang.")
//    if (typeWithoutJavaLang in javaPrimitives) {
//        val suffix = typeWithoutJavaLang.toTitleCase()
//        return primitiveSerializationMethod(suffix)
//    }
//
//
//    // Kotlin primitive types
//    val typeWithoutKotlin = type.removePrefix("kotlin.")
//    if (typeWithoutKotlin in kotlinPrimitives) {
//        return primitiveSerializationMethod(typeWithoutKotlin)
//    }
//    // Boolean (special case)
//    if (typeWithoutKotlin == "Boolean") {
//        return SerializationMethod(
//                serialization = "put(\"$name\", if($name) net.minecraft.nbt.ByteTag(1) else net.minecraft.nbt.ByteTag(0))",
//                deserialization = "(getTag(\"$name\") as net.minecraft.nbt.ByteTag).getByte() != 0.toByte()"
//        )
//    }
//
//    // Kotlin array types
//    val typeWithoutArraySurrounding = type.removePrefix("kotlin.Array<kotlin.").removeSuffix(">")
//    if (typeWithoutArraySurrounding in kotlinArrayTypes) {
//        return primitiveSerializationMethod(typeWithoutArraySurrounding + "Array")
//    }
//
//    //UUID
//    if (type == "java.util.UUID") return SerializationMethod(
//            serialization = "put(\"$name\" + \"Most\", net.minecraft.nbt.LongTag($name.getMostSignificantBits()));\n" +
//                    "      put(\"$name\" + \"Least\", net.minecraft.nbt.LongTag($name.getLeastSignificantBits()));",
//            deserialization = "java.util.UUID((getTag(\"$name\" + \"Most\") as net.minecraft.nbt.LongTag).getLong(), (getTag(\"$name\" + \"Least\") as net.minecraft.nbt.LongTag).getLong())"
//    )
//    // BlockPos
//    if (type == "net.minecraft.util.math.BlockPos") {
//        return SerializationMethod(
//                serialization = "put(\"$name\", net.minecraft.nbt.LongTag($name.toLong()))",
//                deserialization = "net.minecraft.util.math.BlockPos.fromLong((getTag(\"$name\") as net.minecraft.nbt.LongTag).getLong())"
//        )
//    }
//
//    // Custom serializables
//    if (type in serializables) {
//        val deserializer = "${type.simpleName()}$DeserializerSuffix"
//        return SerializationMethod(
//                serialization = "put(\"$name\", $name.toTag())",
//                deserialization = "$deserializer.fromTag(getTag(\"$name\") as CompoundTag)"
//        )
//    }
//
//    if (type.startsWith("java.util.List")) {
//        val genericType = type.removePrefix("java.util.List<").removeSuffix(">")
//        return listSerializationMethod(
//                genericType, serializables, className, processingEnv, element, nestingLevel + 1
//        )
//    }
//
//
//    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Hello there." +
//            " It appears you tried to put @NbtSerializable on a class named '$className' with a property that is not serializable named '$name'." +
//            " Make sure every property is either: A Primitive, UUID, BlockPos, or your own class that is annotated with @NbtSerializable." +
//            " ByteArray, IntArray and LongArray, and a list of any NbtSerializable also work out of the box.\n" +
//            "If you are certain this property is serializable, please submit an issue on Github.\n" +
//            "(Could not find type $type in existing serializables: $serializables)", element)
//
//    return SerializationMethod("", "")
//
////    throw AnnotationProcessingException(
////    )
//
//
//}


//fun SerializableProperty.getPutStatement(serializables: Set<String>, className: String): String {
//    println("Processing $this")
//    // Java types
//    val typeWithoutJavaLang = type.removePrefix("java.lang.")
//    if (typeWithoutJavaLang in javaPrimitives) return "put${typeWithoutJavaLang.toTitleCase()}${putArgs(name, name)}"
//
//    // Kotlin primitive types
//    val typeWithoutKotlin = type.removePrefix("kotlin.")
//    if (typeWithoutKotlin in kotlinPrimitives) return "put$typeWithoutKotlin${putArgs(name)}"
//
//    // Kotlin array types
//    val typeWithoutArraySurrounding = type.removePrefix("kotlin.Array<kotlin.").removeSuffix(">")
//    if (typeWithoutArraySurrounding in kotlinArrayTypes) return "put${typeWithoutArraySurrounding}Array${putArgs(name)}"
//
//    //UUID
//    if (type == "java.util.UUID") return "putUuid${putArgs(name)}"
//
//
//    // Custom serializables
//    if (type in serializables) return "put" + putArgs(name, "$name.toTag()")
//
//
//
//
//    throw AnnotationProcessingException("Hello there." +
//            " It appears you tried to put @NbtSerializable on a class named '$className' with a property that is not serializable named '$name'." +
//            " Make sure every property is either: A Primitive, UUID, BlockPos, or your own class that is annotated with @NbtSerializable." +
//            " ByteArray, IntArray and LongArray also work out of the box.\n" +
//            "If you are certain this property is serializable, please submit an issue on Github.\n" +
//            "(Could not find type $type in existing serializables: $serializables)"
//    )
//
//}


//data class SerializableClass(val fields : List<String>)