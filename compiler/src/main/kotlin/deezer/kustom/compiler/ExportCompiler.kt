package deezer.kustom.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import deezer.kustom.KustomExport
import deezer.kustom.compiler.js.pattern.`class`.parseClass
import deezer.kustom.compiler.js.pattern.`class`.transform
import deezer.kustom.compiler.js.pattern.`interface`.parseInterface
import deezer.kustom.compiler.js.pattern.`interface`.transform
import deezer.kustom.compiler.js.pattern.enum.parseEnum
import deezer.kustom.compiler.js.pattern.enum.transform
import kotlin.random.Random

// Trick to share the Logger everywhere without injecting the dependency everywhere
internal lateinit var sharedLogger: KSPLogger

object Logger : KSPLogger by sharedLogger

@KotlinPoetKspPreview
class ExportCompiler(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    init {
        sharedLogger = environment.logger
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val passId = Random.nextLong()
        val symbols = try {
            resolver.getSymbolsWithAnnotation(environment.options["annotation"] ?: KustomExport::class.qualifiedName!!)
        } catch (e: Exception) {
            devLog("WTF? ${e.message} // ${e.stackTraceToString()}")
            return emptyList()
        }
        devLog("passId: $passId - symbols: ${symbols.count()} - first: ${symbols.firstOrNull()?.location}")

        // ------------------------------------------------------------------------------------------------
        // Hack to avoid compilation on Android/iOS : create a dummy file, then check generated file path
        // https://github.com/google/ksp/issues/641
        symbols.firstOrNull()?.accept(object : KSVisitorVoid() {
            // For some reasons on KSP 1.5.31-1.0 not using the Visitor pattern lead to gradle freeze
            override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                devLog("Creating the placeholder...")
                FileSpec.builder("", "placeholder")
                    .build().writeCode(environment)
            }
        }, Unit)
        val generatedPath = environment.codeGenerator.generatedFile.firstOrNull().toString()
        val isJsBuild = generatedPath.contains("/jsMain/")
        // Please don't ask why there is multiple possible values here, I've no clue, but it's working
        val isUnitTest =
            generatedPath.contains("/T/junit") || generatedPath.contains("/T/Kotlin-Compilation") || generatedPath == "null"
        devLog("isJsBuild=$isJsBuild isUnitTest=$isUnitTest generatedPath=$generatedPath")
        if (!isUnitTest && !isJsBuild) return emptyList() // Disable compilation
        // ------------------------------------------------------------------------------------------------

        symbols
            // Filter when dev is ended, for now we want to log everything
            /*.filter { it is KSClassDeclaration && it.validate() }*/
            .forEach {
                devLog("----- Symbol $it")
                it.accept(ExportVisitor(), Unit)
                //it.accept(LoggerVisitor(environment), Unit)
            }

        return emptyList()// ret
    }

    @KotlinPoetKspPreview
    inner class ExportVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            devLog("----- visitClassDeclaration $classDeclaration - classKind = ${classDeclaration.classKind}")
            when (classDeclaration.classKind) {
                ClassKind.INTERFACE -> {
                    parseInterface(classDeclaration)
                        .transform()
                        .writeCode(environment, classDeclaration.containingFile!!)
                }
                ClassKind.ENUM_CLASS -> {
                    parseEnum(classDeclaration)
                        .transform()
                        .writeCode(environment, classDeclaration.containingFile!!)
                }
                ClassKind.CLASS -> {
                    parseClass(classDeclaration)
                        .transform()
                        .writeCode(environment, classDeclaration.containingFile!!)
                }
                else -> error("The compiler can't handle '${classDeclaration.classKind}' class kind")
            }
        }
    }

    fun devLog(msg: String) {
        println(msg) // for unit tests
        environment.logger.warn(msg) // when compiling other modules
    }
}

@KotlinPoetKspPreview
class ExportCompilerProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        ExportCompiler(environment)
}