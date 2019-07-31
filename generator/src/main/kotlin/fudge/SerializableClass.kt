package fudge

data class SerializableClass(val fields: List<SerializableProperty>)

data class SerializableProperty(val name: String, val type: String)

fun SerializableProperty.getPutStatement() = when (val typeWithoutJavaLang = type.removePrefix("java.lang.")) {
    in primitives -> "put$typeWithoutJavaLang(\"$name\", $name)"
    else -> throw AnnotationProcessingException("Unsupported. there is no serialization for the type '$type' yet, (ShortName = $typeWithoutJavaLang). ")
}

class AnnotationProcessingException(string: String) : Exception(string)

val primitives = listOf("String", "int", "long", "byte", "short", "float", "double", "boolean")
//TODO: EXTRA PRIMITIVES: uuid, byteArray, intArray, LongArray
//TODO: COMPOUNDS: CompoundTag, ListTag

private fun putPrimitive(name: String) = "put$name(\"$name\", $name)"
private fun primitiveNamed(name: String) = "java.lang.$name"


//data class SerializableClass(val fields : List<String>)