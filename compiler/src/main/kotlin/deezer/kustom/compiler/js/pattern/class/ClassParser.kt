package deezer.kustom.compiler.js.pattern.`class`

import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.ParameterDescriptor
import deezer.kustom.compiler.js.pattern.parseFunctions
import deezer.kustom.compiler.js.pattern.parseProperties
import deezer.kustom.compiler.js.pattern.toTypeNamePatch
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName

@KotlinPoetKspPreview
fun parseClass(classDeclaration: KSClassDeclaration): ClassDescriptor {
    val constructorParams = classDeclaration.primaryConstructor?.parameters?.map {
        ParameterDescriptor(
            name = it.name!!.asString(),
            type = it.type.toTypeNamePatch()
        )
    } ?: emptyList()

    if (classDeclaration.simpleName.asString() == "DeezerMedia") {
        Logger.warn("classDeclaration.getAllSuperTypes()")
        classDeclaration.getAllSuperTypes().forEach {
            Logger.warn(" - $it -> ${it.toTypeName()}")
        }

        //Logger.error("END")
    }


    return ClassDescriptor(
        packageName = classDeclaration.packageName.asString(),
        classSimpleName = classDeclaration.simpleName.asString(),
        superTypes = classDeclaration.getAllSuperTypes().map { it.toTypeNamePatch(classDeclaration.containingFile) }
            .toList(),
        constructorParams = constructorParams,
        properties = classDeclaration.parseProperties(),
        functions = classDeclaration.parseFunctions(),
    )
}
