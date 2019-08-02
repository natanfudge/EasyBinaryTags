package fudge

data class SerializableClass(val fields: List<SerializableProperty>)

data class SerializableProperty(val name: String, val type: String)


data class MethodSuffixAndArgsResult(val suffix: String, val propertyName: String, val simpleTypeName: String) {
    val isTag get() = suffix == ""
}

private fun result(propertyName: String, type: String, suffix: String)
        = MethodSuffixAndArgsResult(suffix, propertyName, type.split(".").last())

//TODO: lists

/**
 * Returns int("age", age), ByteArray("parentAges",parentAges), etc
 * Returns  MethodSuffixAndArgsResult instead of a string so we can know later if need to add "tag" to "put" so we can have "putTag"
 */
//TODO: change this to put(key, new XTag(x)) so it works better with lists, but be careful because sometimes the implementation is different.
// Also no need for MethodSuffixAndArgsResult in that case I think
fun SerializableProperty.getMethodSuffixAndArgs(serializables: Set<String>, className: String): MethodSuffixAndArgsResult {
    println("Processing $this")
    // Java types
    val typeWithoutJavaLang = type.removePrefix("java.lang.")
    if (typeWithoutJavaLang in javaPrimitives) return result(name, type, suffix = typeWithoutJavaLang.toTitleCase())

    // Kotlin primitive types
    val typeWithoutKotlin = type.removePrefix("kotlin.")
    if (typeWithoutKotlin in kotlinPrimitives) return result(name, type, suffix = typeWithoutKotlin)

    // Kotlin array types
    val typeWithoutArraySurrounding = type.removePrefix("kotlin.Array<kotlin.").removeSuffix(">")
    if (typeWithoutArraySurrounding in kotlinArrayTypes) {
        return result(name, type, suffix = typeWithoutArraySurrounding + "Array")
    }

    //UUID
    if (type == "java.util.UUID") return result(name, type, suffix = "Uuid")


    // Custom serializables
    if (type in serializables) return result(name, type, suffix = "")




    throw AnnotationProcessingException("Hello there." +
            " It appears you tried to put @NbtSerializable on a class named '$className' with a property that is not serializable named '$name'." +
            " Make sure every property is either: A Primitive, UUID, BlockPos, or your own class that is annotated with @NbtSerializable." +
            " ByteArray, IntArray and LongArray also work out of the box.\n" +
            "If you are certain this property is serializable, please submit an issue on Github.\n" +
            "(Could not find type $type in existing serializables: $serializables)"
    )
}

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

fun String.toTitleCase() = this[0].toUpperCase() + this.substring(1, length)

class AnnotationProcessingException(string: String) : Exception(string)

val javaPrimitives = listOf("String"/*,"byte", "short", "float", "double", "boolean",  "int", "long"*/)
val kotlinPrimitives = listOf("Byte", "Short", "Float", "Double", "Boolean", "String", "Int", "Long")
val kotlinArrayTypes = listOf("Byte", "Int", "Long")
//TODO: EXTRA PRIMITIVES: uuid, byteArray, intArray, LongArray
//TODO: COMPOUNDS: CompoundTag, ListTag

private fun putPrimitive(name: String) = "put$name(\"$name\", $name)"
private fun primitiveNamed(name: String) = "java.lang.$name"


//data class SerializableClass(val fields : List<String>)