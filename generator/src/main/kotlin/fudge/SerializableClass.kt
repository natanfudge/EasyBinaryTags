package fudge

data class SerializableClass(val fields: List<SerializableProperty>)

data class SerializableProperty(val name: String, val type: String)

private fun putArgs(arg1: String, arg2: String) = "(\"$arg1\", $arg2)"
private fun putArgs(arg1: String) = putArgs(arg1, arg1)

fun SerializableProperty.getPutStatement(serializables : Set<String>): String {
    println("Processing $this")
    // Java types
    val typeWithoutJavaLang = type.removePrefix("java.lang.")
    if (typeWithoutJavaLang in javaPrimitives) return "put${typeWithoutJavaLang.toTitleCase()}${putArgs(name, name)}"

    // Kotlin primitive types
    val typeWithoutKotlin = type.removePrefix("kotlin.")
    if (typeWithoutKotlin in kotlinPrimitives) return "put$typeWithoutKotlin${putArgs(name)}"

    // Kotlin array types
    val typeWithoutArraySurrounding = type.removePrefix("kotlin.Array<kotlin.").removeSuffix(">")
    if (typeWithoutArraySurrounding in kotlinArrayTypes) return "put${typeWithoutArraySurrounding}Array${putArgs(name)}"

    // Custom serializables
    if(type in serializables) return "put" + putArgs(name,"$name.toTag()")



    //UUID
    if (type == "java.util.UUID") return "putUuid${putArgs(name)}"
    else throw AnnotationProcessingException("Unsupported." +
            " there is no serialization for the type '$type' yet, " +
//            "(typeWithoutJavaLang = $typeWithoutJavaLang), " +
            "(typeWithoutKotlin = $typeWithoutKotlin), " +
            "(typeWithoutArraySurrounding) = $typeWithoutArraySurrounding).")

}

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