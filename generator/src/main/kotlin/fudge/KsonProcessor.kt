package fudge

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement


@AutoService(Processor::class)
class KsonProcessor : AbstractProcessor() {

    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        println("getSupportedAnnotationTypes")
        return mutableSetOf(Kson::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        println("process")
        roundEnv.getElementsAnnotatedWith(Kson::class.java)
                .forEach {
                    println("Processing: ${it.simpleName}")
                    val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                    generateClass(it, pack)
                }
        return true
    }

    private fun generateClass(element: Element, pack: String) {

        val fileName = "${element.simpleName}KsonUtil"

        val serializable = element.toSerializableClass()
        //TODO: imports
        //TODO differ between int,string, long etc and non primitives
        val putStatements = serializable.fields
                .joinToString("\n    "){ it.getPutStatement()}
        val string = """
return CompoundTag().apply {
    $putStatements
}
        """.trimIndent()

        val file = KotlinPoet.file(pack, fileName){
//            addImport(CompoundTagNamespace, CompoundTagName)
            addFunction(name = "toTag"){
                returns("CompoundTag".toClassName(packageName = CompoundTagNamespace))
                receiver(element.asType().asTypeName())
                addStatement(string)
            }
        }


        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

    private fun Element.toSerializableClass()  : SerializableClass{
        val fields = enclosedElements
                .filter { it.kind == ElementKind.FIELD }
                .map {
                     SerializableProperty(it.toString(),it.asType().asTypeName().toString())
                }

        return SerializableClass(fields)

    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val CompoundTagNamespace = "fudge.minecraft"
        const val CompoundTagName = "CompoundTag"
    }
}