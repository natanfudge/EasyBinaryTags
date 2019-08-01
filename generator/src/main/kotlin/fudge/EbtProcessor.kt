package fudge

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

//TODO: support incremental processing somehow (The problem is that old source files don't get deleted)
//TODO: tone down logging for the user


//TODO: BlockPos serializing and testing in the final version
@AutoService(Processor::class)
class EbtProcessor : AbstractProcessor() {

    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        println("getSupportedAnnotationTypes")
        return mutableSetOf(NbtSerializable::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val elements = roundEnv.getElementsAnnotatedWith(NbtSerializable::class.java)
        // See what has been annotated so we can tell if a class that is declared in a field of a serializable class is missing
        val serializables = elements.map { it.simpleName.toString() }.toSet()


        for (element in elements) {
            println("Processing: ${element.simpleName}")

            println()

//                    val pack = processingEnv.elementUtils.getPackageOf(it)
            //TODO: think about if this is optimal
            generateClass(element, "", serializables)
        }

        return true
    }

    private fun generateClass(element: Element, pack: String, serializables: Set<String>) {

        val fileName = "${element.simpleName}NbtSerializer"
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Generating class in file '$fileName' in package '$pack'\n")

        val serializableClass = element.toSerializableClass()
        val putStatements = serializableClass.fields
                .joinToString("\n    ") { it.getPutStatement(serializables) }
        val string = """
return CompoundTag().apply {
    $putStatements
}
        """.trimIndent()

        val file = KotlinPoet.file(pack, fileName) {
            addFunction(name = "toTag") {
                returns("CompoundTag".toClassName(packageName = CompoundTagNamespace))
                receiver(element.asType().asTypeName())
                addStatement(string)
            }
        }


        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

//    data class X(val Me :Int)

    private fun Element.toSerializableClass(): SerializableClass {
//        println("elements = ${enclosedElements}")
        val publicMethods = enclosedElements
                .filter { it.kind == ElementKind.METHOD && it.modifiers.contains(Modifier.PUBLIC) }
                .map { it.toString().removeSuffix("()") }
//        println("methods = $publicMethods")

        val fields = enclosedElements
                .filter {
                    it.kind == ElementKind.FIELD
                            //Ensure private fields don't get in
                            &&  it.hasGetter(publicMethods)

                }
                .map {
                    SerializableProperty(it.toString(), it.asType().asTypeName().toString())
                }

        return SerializableClass(fields)

    }

    private fun Element.hasGetter(methods: List<String>)  :Boolean{
        val getterName = if (this.toString().startsWith("is")) this.toString() else "get" + this.toString().toTitleCase()
//        println("getterName = $getterName")
        return getterName in methods
    }

    private fun Element.isBoolean() = asType().asTypeName().toString() == "kotlin.Boolean"

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val CompoundTagNamespace = "fudge.minecraft"
        const val CompoundTagName = "CompoundTag"
    }
}