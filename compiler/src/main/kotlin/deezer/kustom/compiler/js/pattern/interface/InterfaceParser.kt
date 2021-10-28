package deezer.kustom.compiler.js.pattern.`interface`

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.pattern.parseFunctions
import deezer.kustom.compiler.js.pattern.parseProperties
import deezer.kustom.compiler.js.pattern.toTypeNamePatch

@KotlinPoetKspPreview
fun parseInterface(classDeclaration: KSClassDeclaration): InterfaceDescriptor {
    if (classDeclaration.simpleName.asString() == "RepeatModeChangedEventListener") {
        Logger.warn("classDeclaration.getDeclaredFunctions()")
        classDeclaration.getDeclaredFunctions().forEach {
            Logger.warn(" - ${it.simpleName.asString()}")
        }

        Logger.warn("classDeclaration.getAllFunctions()")
        classDeclaration.getAllFunctions().forEach {
            Logger.warn(" - ${it.simpleName.asString()}")
        }
    }
    return InterfaceDescriptor(
        packageName = classDeclaration.packageName.asString(),
        classSimpleName = classDeclaration.simpleName.asString(),
        properties = classDeclaration.parseProperties(),
        superTypes = classDeclaration.getAllSuperTypes().map { it.toTypeNamePatch(classDeclaration.containingFile) }
            .toList(),
        functions = classDeclaration.parseFunctions()
    )
}